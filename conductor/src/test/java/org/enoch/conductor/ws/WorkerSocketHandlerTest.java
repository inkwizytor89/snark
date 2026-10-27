package org.enoch.conductor.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.enoch.common.status.NavigatorStatus;
import org.enoch.common.status.ServerStatus;
import org.enoch.common.status.StatusDetails;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.any;
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
                 "timestamp":2,"status":200,"payload":{"instanceId":"worker#1","state":"running",
                 "serverStatus":{"navigatorStatus":{"fleetStatus":"Unsolicited status","lastUpdate":"2026-10-21T12:00:00"},
                 "activeThreads":[],"commandDeque":"","notifications":""}}}
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
                 "timestamp":2,"status":200,"payload":{"instanceId":"worker#1","state":"running",
                 "serverStatus":{"navigatorStatus":{"fleetStatus":"Runtime is healthy","lastUpdate":"2026-10-21T12:34:56"},
                 "activeThreads":["global:consumer"],
                 "commandDeque":"Nhash","notifications":"global.consumer\\nfailure\\nstack trace",
                 "coordinates":{"planetCount":1,"moonCount":1,"colonies":[
                 {"coordinate":"p[1:2:3]","metal":100,"crystal":200,"deuterium":300,"energy":400,
                 "ships":{"battleship":5}}]}}}}
                """.replace("${requestId}", requestJson.path("id").asText()));

        assertEquals("Runtime is healthy", handler.getLastStatusMessage("worker#1"));
        ServerStatus serverStatus = handler.getLastServerStatus("worker#1");
        assertEquals("Runtime is healthy", serverStatus.getNavigatorStatus().getFleetStatus());
        assertEquals("2026-10-21T12:34:56", serverStatus.getNavigatorStatus().getLastUpdate().toString());
        assertEquals("global:consumer", serverStatus.getActiveThreads().get(0));
        assertEquals("Nhash", serverStatus.getCommandDeque());
        assertEquals("global.consumer\nfailure\nstack trace", serverStatus.getNotifications());
        assertEquals(1, serverStatus.getCoordinates().getPlanetCount());
        assertEquals(1, serverStatus.getCoordinates().getMoonCount());
        assertEquals("p[1:2:3]", serverStatus.getCoordinates().getColonies().getFirst().getCoordinate());
        assertEquals(5L, serverStatus.getCoordinates().getColonies().getFirst().getShips().get("battleship"));
        assertTrue(handler.getLastStatusUpdatedAt("worker#1") > 0);
    }

    @Test
    void shouldAcceptLegacyStatusResponseDuringWorkerUpgrade() throws Exception {
        WorkerSocketHandler handler = new TestWorkerSocketHandler();
        WebSocketSession session = openSession();
        TestWorkerSocketHandler testHandler = (TestWorkerSocketHandler) handler;
        testHandler.receive(session, """
                {"id":"registration-1","type":"request:register","from":"worker#1","to":"conductor",
                 "timestamp":1,"status":100,"payload":{"instanceId":"worker#1"}}
                """);
        clearInvocations(session);
        assertTrue(handler.requestStatus("worker#1"));
        ArgumentCaptor<TextMessage> request = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(request.capture());
        String requestId = mapper.readTree(request.getValue().getPayload()).path("id").asText();

        testHandler.receive(session, """
                {"id":"%s","type":"response:status","from":"worker#1","to":"conductor",
                 "timestamp":2,"status":200,
                 "payload":{"instanceId":"worker#1","state":"running","message":"F:0/1 E:0/0"}}
                """.formatted(requestId));

        ServerStatus status = handler.getLastServerStatus("worker#1");
        assertEquals("F:0/1 E:0/0", status.getNavigatorStatus().getFleetStatus());
        assertNull(status.getNavigatorStatus().getLastUpdate());
        assertTrue(status.getActiveThreads().isEmpty());
        assertEquals("", status.getCommandDeque());
        assertTrue(handler.getLastStatusUpdatedAt("worker#1") > 0);
    }

    @Test
    void shouldAcceptUnsolicitedStatusUpdatesAndIgnoreStaleRevisions() throws Exception {
        WorkerSocketHandler handler = new TestWorkerSocketHandler();
        WebSocketSession session = openSession();
        TestWorkerSocketHandler testHandler = (TestWorkerSocketHandler) handler;
        testHandler.receive(session, """
                {"id":"registration-1","type":"request:register","from":"worker#1","to":"conductor",
                 "timestamp":1,"status":100,"payload":{"instanceId":"worker#1"}}
                """);

        testHandler.receive(session, """
                {"id":"update-2","type":"update:status","from":"worker#1","to":"conductor",
                 "timestamp":2,"status":200,"payload":{"instanceId":"worker#1","revision":2,
                 "state":"running","serverStatus":{"navigatorStatus":{"fleetStatus":"new status",
                 "lastUpdate":"2026-10-21T12:00:00"},"activeThreads":[],"commandDeque":"",
                 "notifications":"","coordinates":{"planetCount":0,"moonCount":0,"colonies":[]}}}}
                """);
        long updatedAt = handler.getLastStatusUpdatedAt("worker#1");
        testHandler.receive(session, """
                {"id":"update-1","type":"update:status","from":"worker#1","to":"conductor",
                 "timestamp":1,"status":200,"payload":{"instanceId":"worker#1","revision":1,
                 "state":"running","serverStatus":{"navigatorStatus":{"fleetStatus":"stale status",
                 "lastUpdate":"2026-10-21T11:00:00"},"activeThreads":[],"commandDeque":"",
                 "notifications":"","coordinates":{"planetCount":0,"moonCount":0,"colonies":[]}}}}
                """);

        assertEquals("new status", handler.getLastStatusMessage("worker#1"));
        assertEquals(updatedAt, handler.getLastStatusUpdatedAt("worker#1"));
    }

    @Test
    void shouldRequestAndValidateStatusDetailsPage() throws Exception {
        WorkerSocketHandler handler = new TestWorkerSocketHandler();
        WebSocketSession session = openSession();
        TestWorkerSocketHandler testHandler = (TestWorkerSocketHandler) handler;
        testHandler.receive(session, """
                {"id":"registration-1","type":"request:register","from":"worker#1","to":"conductor",
                 "timestamp":1,"status":100,"payload":{"instanceId":"worker#1"}}
                """);
        doAnswer(invocation -> {
            TextMessage sentMessage = invocation.getArgument(0);
            JsonNode request = mapper.readTree(sentMessage.getPayload());
            testHandler.receive(session, """
                    {"id":"%s","type":"response:status:details","from":"worker#1","to":"conductor",
                     "timestamp":2,"status":200,"payload":{"instanceId":"worker#1","details":{
                     "offset":0,"limit":25,"total":1,"planetCount":1,"moonCount":0,
                     "colonies":[{"coordinate":"p[1:2:3]","metal":100,"crystal":200,"deuterium":300,
                     "energy":400,"ships":{"battleship":5}}]}}}
                    """.formatted(request.path("id").asText()));
            return null;
        }).when(session).sendMessage(any(TextMessage.class));

        StatusDetails details = handler.requestStatusDetails("worker#1", 0, 25);

        assertEquals(1, details.total());
        assertEquals("p[1:2:3]", details.colonies().getFirst().getCoordinate());
    }

    @Test
    void shouldCloseSessionsSendingOversizedWorkerMessages() throws Exception {
        WorkerSocketHandler handler = new TestWorkerSocketHandler();
        WebSocketSession session = openSession();
        ((TestWorkerSocketHandler) handler).receive(session,
                "{\"type\":\"LOG\"}" + " ".repeat(16 * 1024));

        verify(session).close(CloseStatus.TOO_BIG_TO_PROCESS);
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
