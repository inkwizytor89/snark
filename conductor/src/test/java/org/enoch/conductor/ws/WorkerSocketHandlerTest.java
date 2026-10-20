package org.enoch.conductor.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.clearInvocations;

class WorkerSocketHandlerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldRegisterWorkerAndAcknowledgeRegistrationRequest() throws Exception {
        WorkerSocketHandler handler = new TestWorkerSocketHandler();
        WebSocketSession session = openSession();
        String registration = """
                {"id":"registration-1","type":"request:register","from":"worker#1","to":"conductor",
                 "timestamp":1,"status":100,"payload":{"instanceId":"worker#1","version":"test","hostname":"host"}}
                """;

        ((TestWorkerSocketHandler) handler).receive(session, registration);

        assertTrue(handler.isRegistered("worker#1"));
        ArgumentCaptor<TextMessage> sent = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(sent.capture());
        JsonNode response = mapper.readTree(sent.getValue().getPayload());
        assertEquals("registration-1", response.path("id").asText());
        assertEquals("response:register", response.path("type").asText());
        assertEquals("conductor", response.path("from").asText());
        assertEquals("worker#1", response.path("to").asText());
        assertTrue(response.path("payload").path("registered").asBoolean());
    }

    @Test
    void shouldRequireRegisteredSessionAndCorrelatedStatusResponse() throws Exception {
        WorkerSocketHandler handler = new TestWorkerSocketHandler();
        WebSocketSession session = openSession();
        TestWorkerSocketHandler testHandler = (TestWorkerSocketHandler) handler;
        String uncorrelatedStatus = """
                {"id":"status-1","type":"response:status","from":"worker#1","to":"conductor",
                 "timestamp":2,"status":200,"payload":{"instanceId":"worker#1","state":"running","message":"Unsolicited status"}}
                """;
        testHandler.receive(session, uncorrelatedStatus);
        assertNull(handler.getLastStatusMessage("worker#1"));

        testHandler.receive(session, """
                {"id":"registration-1","type":"request:register","from":"worker#1","to":"conductor",
                 "timestamp":1,"status":100,"payload":{"instanceId":"worker#1"}}
                """);
        clearInvocations(session);
        assertTrue(handler.requestStatus("worker#1"));
        ArgumentCaptor<TextMessage> request = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(request.capture());
        JsonNode requestJson = mapper.readTree(request.getValue().getPayload());
        assertEquals("request:status", requestJson.path("type").asText());
        assertEquals("conductor", requestJson.path("from").asText());
        assertEquals("worker#1", requestJson.path("to").asText());

        testHandler.receive(session, uncorrelatedStatus.replace("status-1", "unknown-request"));
        assertNull(handler.getLastStatusMessage("worker#1"));
        testHandler.receive(session, """
                {"id":"${requestId}","type":"response:status","from":"worker#1","to":"conductor",
                 "timestamp":2,"status":200,"payload":{"instanceId":"worker#1","state":"running","message":"Runtime is healthy"}}
                """.replace("${requestId}", requestJson.path("id").asText()));

        assertEquals("Runtime is healthy", handler.getLastStatusMessage("worker#1"));
        assertTrue(handler.getLastStatusUpdatedAt("worker#1") > 0);
    }

    @Test
    void shouldRejectRegistrationWhenPayloadIdentityDoesNotMatchSender() throws Exception {
        WorkerSocketHandler handler = new TestWorkerSocketHandler();
        WebSocketSession session = openSession();
        ((TestWorkerSocketHandler) handler).receive(session, """
                {"id":"registration-1","type":"request:register","from":"worker#1","to":"conductor",
                 "timestamp":1,"status":100,"payload":{"instanceId":"worker#2"}}
                """);

        assertFalse(handler.isRegistered("worker#1"));
        assertFalse(handler.isRegistered("worker#2"));
        ArgumentCaptor<TextMessage> sent = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(sent.capture());
        assertEquals("error:register", mapper.readTree(sent.getValue().getPayload()).path("type").asText());
    }

    private WebSocketSession openSession() {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("session-1");
        when(session.isOpen()).thenReturn(true);
        return session;
    }

    private static class TestWorkerSocketHandler extends WorkerSocketHandler {
        void receive(WebSocketSession session, String payload) throws Exception {
            super.handleTextMessage(session, new TextMessage(payload));
        }
    }
}
