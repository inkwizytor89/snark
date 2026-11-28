package org.enoch.snark.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.enoch.common.command.CommandMessage;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.common.status.ColonyStatus;
import org.enoch.common.status.NavigatorStatus;
import org.enoch.common.status.ServerStatus;
import org.enoch.common.status.StatusDetails;
import org.enoch.snark.config.ConfigService;
import org.enoch.common.config.ConfigEntry;
import org.enoch.snark.instance.si.Core;
import org.enoch.snark.instance.si.ThreadManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Map;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

        client.publishRunningState();
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
    void shouldReportStartingStateInStatusResponse() {
        client.publishStartingState();
        CommandMessage incoming = new CommandMessage();
        incoming.setId("status-starting");
        incoming.setType("request:status");
        incoming.setFrom("conductor");
        incoming.setTo("worker#1");

        CommandMessage response = client.buildStatusResponse(incoming);

        assertEquals("starting", ((Map<?, ?>) response.getPayload()).get("state"));
    }

    @Test
    void shouldBuildCorrelatedStopResponse() {
        CommandMessage request = new CommandMessage();
        request.setId("stop-1");
        request.setType("request:stop");
        request.setFrom("conductor");
        request.setTo("worker#1");

        CommandMessage response = client.buildStopResponse(request);

        assertEquals("stop-1", response.getId());
        assertEquals("response:stop", response.getType());
        assertEquals("worker#1", response.getFrom());
        assertEquals("conductor", response.getTo());
        assertEquals(200, response.getStatus());
        assertEquals(Boolean.TRUE, ((Map<?, ?>) response.getPayload()).get("accepted"));
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

    @Test
    void shouldBuildCorrelatedManagedThreadConfigurationResponse() {
        CommandMessage request = new CommandMessage();
        request.setId("thread-config-1");
        request.setType("request:thread:configurations");
        request.setFrom("conductor");
        request.setTo("worker#1");

        CommandMessage response = client.buildThreadConfigurationResponse(request, java.util.List.of(
                new ThreadDefinition("fleet", "fleet", "Fleet configuration", java.util.List.of(
                        new ConfigDefinition("mission", "ATTACK", false, "")))));

        assertEquals("thread-config-1", response.getId());
        assertEquals("response:thread:configurations", response.getType());
        assertEquals("worker#1", response.getFrom());
        assertEquals("conductor", response.getTo());
        assertEquals(200, response.getStatus());
        Map<?, ?> payload = (Map<?, ?>) response.getPayload();
        assertEquals("worker#1", payload.get("instanceId"));
        java.util.List<?> definitions = (java.util.List<?>) payload.get("definitions");
        ThreadDefinition definition = (ThreadDefinition) definitions.getFirst();
        assertEquals("fleet", definition.getName());
        assertEquals("ATTACK", definition.getConfigs().getFirst().getValue());
        java.util.List<?> available = (java.util.List<?>) payload.get("availableTypes");
        assertFalse(available.isEmpty());
    }

    @Test
    void shouldReadDatabaseThreadDefinitionsWhenBuildingRequestedResponse() {
        ObjectProvider<ConfigService> provider = mock(ObjectProvider.class);
        ConfigService configService = mock(ConfigService.class);
        when(provider.getObject()).thenReturn(configService);
        when(configService.listThreadDefinitions()).thenReturn(java.util.List.of(
                new ThreadDefinition("fleet", "fleet", "Fleet configuration", java.util.List.of(
                        new ConfigDefinition("mission", "ATTACK", false, "")))));
        when(configService.listModuleDefinitions()).thenReturn(java.util.List.of());
        client.setConfigServiceProvider(provider);
        CommandMessage request = new CommandMessage();
        request.setId("thread-config-2");
        request.setType("request:thread:configurations");
        request.setFrom("conductor");
        request.setTo("worker#1");

        CommandMessage response = client.buildManagedThreadConfigurationResponse(request);

        assertEquals("response:thread:configurations", response.getType());
        ThreadDefinition definition = (ThreadDefinition) ((java.util.List<?>)
                ((Map<?, ?>) response.getPayload()).get("definitions")).getFirst();
        assertEquals("ATTACK", definition.getConfigs().getFirst().getValue());
        org.mockito.Mockito.verify(configService).listThreadDefinitions();
        org.mockito.Mockito.verify(configService).listModuleDefinitions();
    }

    @Test
    void shouldConvertRequestedConfigEntriesAndUpdateThreads() {
        ObjectProvider<ConfigService> provider = mock(ObjectProvider.class);
        ConfigService configService = mock(ConfigService.class);
        when(provider.getObject()).thenReturn(configService);
        client.setConfigServiceProvider(provider);

        CommandMessage request = new CommandMessage();
        request.setId("config-set-1");
        request.setType("request:config:set");
        request.setFrom("conductor");
        request.setTo("worker#1");
        request.setPayload(java.util.List.of(Map.of(
                "module", "fleet",
                "thread", "UpdateThread",
                "key", "refresh",
                "value", "45 seconds")));

        CommandMessage response = client.buildConfigSetResponse(request);

        assertEquals("response:config:set", response.getType());
        assertEquals("config-set-1", response.getId());
        assertEquals(200, response.getStatus());
        ConfigDefinition definition = (ConfigDefinition) ((java.util.List<?>) response.getPayload()).getFirst();
        assertEquals("refresh", definition.getKey());
        assertEquals("45 seconds", definition.getValue());
        org.mockito.ArgumentCaptor<java.util.List<ConfigEntry>> terms =
                org.mockito.ArgumentCaptor.forClass(java.util.List.class);
        org.mockito.Mockito.verify(configService).updateThreads(terms.capture());
        assertEquals(1, terms.getValue().size());
        assertEquals("fleet.UpdateThread.refresh=45 seconds", terms.getValue().getFirst().toString());
    }

    @Test
    void shouldRejectMalformedConfigEntriesWithoutUpdatingThreads() {
        ObjectProvider<ConfigService> provider = mock(ObjectProvider.class);
        ConfigService configService = mock(ConfigService.class);
        when(provider.getObject()).thenReturn(configService);
        client.setConfigServiceProvider(provider);

        CommandMessage request = new CommandMessage();
        request.setId("config-set-2");
        request.setType("request:config:set");
        request.setFrom("conductor");
        request.setTo("worker#1");
        request.setPayload(java.util.List.of(Map.of(
                "module", "fleet",
                "thread", "",
                "key", "refresh",
                "value", "45 seconds")));

        CommandMessage response = client.buildConfigSetResponse(request);

        assertEquals("error:config:set", response.getType());
        assertEquals(400, response.getStatus());
        org.mockito.Mockito.verify(configService, org.mockito.Mockito.never()).updateThreads(
                org.mockito.ArgumentMatchers.anyList());
    }
}
