package org.enoch.snark.ws;

import org.enoch.common.command.CommandMessage;
import org.enoch.snark.instance.si.Core;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SnarkWebSocketClientTest {

    private final Core core = mock(Core.class);
    private final SnarkWebSocketClient client = new SnarkWebSocketClient("ws://localhost:8080/ws", "worker#1", core);

    @Test
    void shouldBuildRegistrationMessageWithMatchingIdentity() {
        CommandMessage registration = client.buildRegistrationMessage();

        assertEquals("request:register", registration.getType());
        assertEquals("worker#1", registration.getFrom());
        assertEquals("conductor", registration.getTo());
        assertEquals("worker#1", ((Map<?, ?>) registration.getPayload()).get("instanceId"));
    }

    @Test
    void shouldConvertRequestTypeToResponseType() {
        assertEquals("response:status", client.toResponseType("request:status"));
        assertEquals("response:config:list", client.toResponseType("request:config:list"));
        assertEquals("response:unknown", client.toResponseType(""));
    }

    @Test
    void shouldBuildEchoResponseWithSamePayloadAndMetadata() {
        CommandMessage incoming = new CommandMessage();
        incoming.setId("abc-123");
        incoming.setType("request:config:list");
        incoming.setFrom("conductor");
        incoming.setTo("worker#1");
        incoming.setPayload(Map.of("key", "value"));
        incoming.setMetadata(Map.of("meta", "data"));

        CommandMessage response = client.buildEchoResponse(incoming);

        assertEquals("abc-123", response.getId());
        assertEquals("response:config:list", response.getType());
        assertEquals("worker#1", response.getFrom());
        assertEquals("conductor", response.getTo());
        assertEquals(200, response.getStatus());
        assertSame(incoming.getPayload(), response.getPayload());
        assertSame(incoming.getMetadata(), response.getMetadata());
        assertNull(response.getError());
    }

    @Test
    void shouldBuildStatusResponseWithRuntimeStatus() {
        when(core.status()).thenReturn("F:0/1 E:0/0 global:consumer");
        CommandMessage incoming = new CommandMessage();
        incoming.setId("status-123");
        incoming.setType("request:status");
        incoming.setFrom("conductor");
        incoming.setTo("worker#1");

        CommandMessage response = client.buildStatusResponse(incoming);

        assertEquals("status-123", response.getId());
        assertEquals("response:status", response.getType());
        assertEquals("worker#1", response.getFrom());
        assertEquals("conductor", response.getTo());
        assertEquals(200, response.getStatus());
        assertEquals("running", ((Map<?, ?>) response.getPayload()).get("state"));
        assertEquals("F:0/1 E:0/0 global:consumer", ((Map<?, ?>) response.getPayload()).get("message"));
    }
}
