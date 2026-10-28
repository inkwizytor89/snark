package org.enoch.snark.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.enoch.common.command.CommandMessage;
import org.enoch.common.status.ColonyStatus;
import org.enoch.common.status.NavigatorStatus;
import org.enoch.common.status.ServerStatus;
import org.enoch.common.status.StatusDetails;
import org.enoch.snark.instance.si.Core;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        LocalDateTime lastUpdate = LocalDateTime.of(2026, 10, 21, 12, 34, 56);
        when(core.statusSummary()).thenReturn(new ServerStatus(
                new NavigatorStatus("F:0/1 E:0/0", lastUpdate),
                java.util.List.of("global:consumer"),
                "Nhash",
                "global.consumer\nfailure\nstack trace",
                null
        ));
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
        ServerStatus status = (ServerStatus) ((Map<?, ?>) response.getPayload()).get("serverStatus");
        assertEquals("F:0/1 E:0/0", status.getNavigatorStatus().getFleetStatus());
        assertEquals(lastUpdate, status.getNavigatorStatus().getLastUpdate());
        assertEquals(java.util.List.of("global:consumer"), status.getActiveThreads());
        assertEquals("Nhash", status.getCommandDeque());
        assertEquals("global.consumer\nfailure\nstack trace", status.getNotifications());
        assertNull(status.getCoordinates());

        JsonNode json = new ObjectMapper().findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .valueToTree(response);
        assertEquals("F:0/1 E:0/0", json.path("payload").path("serverStatus")
                .path("navigatorStatus").path("fleetStatus").asText());
        assertEquals("2026-10-21T12:34:56", json.path("payload").path("serverStatus")
                .path("navigatorStatus").path("lastUpdate").asText());
        assertEquals("Nhash", json.path("payload").path("serverStatus").path("commandDeque").asText());
        assertTrue(json.path("payload").path("serverStatus").path("coordinates").isNull());
    }

    @Test
    void shouldBuildUnsolicitedStatusUpdateWithRevision() {
        when(core.statusSummary()).thenReturn(new ServerStatus(
                new NavigatorStatus("F:0/1 E:0/0", LocalDateTime.of(2026, 10, 21, 12, 34, 56)),
                java.util.List.of("global:consumer"),
                "Nhash",
                "short notification",
                null
        ));

        CommandMessage update = client.buildStatusUpdate();

        assertEquals("update:status", update.getType());
        assertEquals("worker#1", update.getFrom());
        assertEquals("conductor", update.getTo());
        assertEquals(200, update.getStatus());
        assertEquals(1L, ((Map<?, ?>) update.getPayload()).get("revision"));
    }

    @Test
    void shouldBuildRestartRequiredEvent() throws Exception {
        CommandMessage event = client.buildRestartRequiredEvent("ChromeDriver window is unavailable");

        assertTrue(event.getId() != null && !event.getId().isBlank());
        assertEquals("event:restart-required", event.getType());
        assertEquals("worker#1", event.getFrom());
        assertEquals("conductor", event.getTo());
        assertEquals(200, event.getStatus());
        JsonNode json = new ObjectMapper().valueToTree(event);
        assertEquals("ChromeDriver window is unavailable",
                json.path("payload").path("reason").asText());
    }

    @Test
    void shouldBuildCorrelatedStatusDetailsResponse() {
        CommandMessage request = new CommandMessage();
        request.setId("details-1");
        request.setType("request:status:details");
        request.setFrom("conductor");
        request.setTo("worker#1");

        CommandMessage response = client.buildStatusDetailsResponse(request,
                new StatusDetails(0, 25, 1, 1, 0,
                        java.util.List.of(new ColonyStatus("p[1:2:3]", 100L, 200L, 300L, 400L,
                                Map.of("battleship", 5L)))));

        assertEquals("details-1", response.getId());
        assertEquals("response:status:details", response.getType());
        assertEquals("worker#1", response.getFrom());
        assertEquals("conductor", response.getTo());
        assertEquals(200, response.getStatus());
        Map<?, ?> payload = (Map<?, ?>) response.getPayload();
        StatusDetails details = (StatusDetails) payload.get("details");
        assertEquals(25, details.limit());
        assertEquals("p[1:2:3]", details.colonies().getFirst().getCoordinate());
    }
}
