package org.enoch.snark.ws;

import org.enoch.common.command.CommandMessage;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SnarkWebSocketClientTest {

    private final SnarkWebSocketClient client = new SnarkWebSocketClient("ws://localhost:8080/ws", "worker#1");

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
}
