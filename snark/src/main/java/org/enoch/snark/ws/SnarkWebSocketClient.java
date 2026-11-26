package org.enoch.snark.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PreDestroy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.enoch.common.command.CommandCatalogResponse;
import org.enoch.common.command.CommandMessage;
import org.enoch.common.command.CommandSubmitRequest;
import org.enoch.common.command.CommandSubmitResponse;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.common.config.ModuleDefinition;
import org.enoch.snark.config.ConfigService;
import org.enoch.common.config.ConfigEntry;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.status.StatusDetails;
import org.enoch.snark.action.command.CommandRecipeService;
import org.enoch.snark.instance.WorkerLifecycle;
import org.enoch.snark.instance.si.Core;
import org.enoch.snark.instance.si.ThreadManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SnarkWebSocketClient {

    private static final int MAX_MESSAGE_BYTES = 16 * 1024;
    private static final int MAX_STATUS_DETAIL_PAGE_SIZE = 25;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final String managerUrl;
    private final String instanceId;
    private final Core core;
    private final CommandRecipeService commandRecipeService;
    private final AtomicLong statusRevision = new AtomicLong();

    private ObjectProvider<WorkerLifecycle> workerLifecycleProvider;
    private ObjectProvider<ThreadManager> threadManagerProvider;
    private ObjectProvider<ConfigService> configServiceProvider;
    private volatile WebSocket webSocket;
    private volatile String registrationRequestId;
    private volatile boolean registered;
    private volatile boolean connectionOpen;
    private volatile boolean connecting;
    private volatile boolean shuttingDown;
    private final AtomicLong stateRevision = new AtomicLong();
    private volatile String runtimeState = "starting";

    public SnarkWebSocketClient(@Value("${managerUrl:}") String managerUrl,
                                @Value("${instanceId:${spring.application.name:snark}}") String instanceId,
                                Core core) {
        this(managerUrl, instanceId, core, null);
    }

    @Autowired
    public SnarkWebSocketClient(@Value("${managerUrl:}") String managerUrl,
                                @Value("${instanceId:${spring.application.name:snark}}") String instanceId,
                                Core core,
                                CommandRecipeService commandRecipeService) {
        this.managerUrl = managerUrl == null ? "" : managerUrl.trim();
        this.instanceId = instanceId == null || instanceId.isBlank() ? "snark" : instanceId.trim();
        this.core = core;
        this.commandRecipeService = commandRecipeService;
    }

    @Autowired
    public void setWorkerLifecycleProvider(ObjectProvider<WorkerLifecycle> workerLifecycleProvider) {
        this.workerLifecycleProvider = workerLifecycleProvider;
    }

    @Autowired
    public void setThreadManagerProvider(ObjectProvider<ThreadManager> threadManagerProvider) {
        this.threadManagerProvider = threadManagerProvider;
    }

    @Autowired
    public void setConfigServiceProvider(ObjectProvider<ConfigService> configServiceProvider) {
        this.configServiceProvider = configServiceProvider;
    }

    @EventListener(ApplicationReadyEvent.class)
    public synchronized void connect() {
        if (managerUrl.isBlank()) {
            System.out.println("[WS][SKIP] managerUrl is empty, WebSocket client not started");
            return;
        }
        if (connectionOpen || connecting || shuttingDown) {
            return;
        }

        Request request = new Request.Builder()
                .url(managerUrl)
                .build();

        connecting = true;
        System.out.println("[WS][CONNECTING] managerUrl=" + managerUrl + " instanceId=" + instanceId);
        webSocket = okHttpClient.newWebSocket(request, new WorkerListener());
    }

    @PreDestroy
    public void shutdown() {
        shuttingDown = true;
        WebSocket current = webSocket;
        if (current != null) {
            current.close(1000, "Application shutdown");
        }
        okHttpClient.dispatcher().executorService().shutdown();
        okHttpClient.connectionPool().evictAll();
    }

    CommandMessage buildRegistrationMessage() {
        CommandMessage message = new CommandMessage();
        message.setId(UUID.randomUUID().toString());
        message.setType("request:register");
        message.setFrom(instanceId);
        message.setTo("conductor");
        message.setTimestamp(System.currentTimeMillis());
        message.setStatus(100);
        message.setPayload(Map.of(
                "instanceId", instanceId,
                "hostname", resolveHostname(),
                "version", resolveVersion()
        ));
        return message;
    }

    CommandMessage buildEchoResponse(CommandMessage incoming) {
        CommandMessage response = new CommandMessage();
        response.setId(incoming.getId());
        response.setType(toResponseType(incoming.getType()));
        response.setFrom(instanceId);
        response.setTo(resolveTarget(incoming));
        response.setTimestamp(System.currentTimeMillis());
        response.setStatus(200);
        response.setPayload(incoming.getPayload());
        response.setMetadata(incoming.getMetadata());
        return response;
    }

    CommandMessage buildStatusResponse(CommandMessage incoming) {
        CommandMessage response = new CommandMessage();
        response.setId(incoming.getId());
        response.setType("response:status");
        response.setFrom(instanceId);
        response.setTo(resolveTarget(incoming));
        response.setTimestamp(System.currentTimeMillis());
        response.setStatus(200);
        response.setPayload(Map.of(
                "instanceId", instanceId,
                "state", "running",
                "revision", statusRevision.incrementAndGet(),
                "serverStatus", core.statusSummary(),
                "hostname", resolveHostname(),
                "version", resolveVersion()
        ));
        return response;
    }

    CommandMessage buildStopResponse(CommandMessage incoming) {
        CommandMessage response = new CommandMessage();
        response.setId(incoming.getId());
        response.setType("response:stop");
        response.setFrom(instanceId);
        response.setTo(resolveTarget(incoming));
        response.setTimestamp(System.currentTimeMillis());
        response.setStatus(200);
        response.setPayload(Map.of("accepted", true));
        return response;
    }

    CommandMessage buildStatusUpdate() {
        CommandMessage update = new CommandMessage();
        update.setId(UUID.randomUUID().toString());
        update.setType("update:status");
        update.setFrom(instanceId);
        update.setTo("conductor");
        update.setTimestamp(System.currentTimeMillis());
        update.setStatus(200);
        String effectiveState = runtimeState;
        if ("running".equals(effectiveState) || "starting".equals(effectiveState) || "logged_out".equals(effectiveState)) {
            effectiveState = effectiveState;
        } else {
            effectiveState = "running";
        }
        update.setPayload(Map.of(
                "instanceId", instanceId,
                "state", effectiveState,
                "revision", statusRevision.incrementAndGet(),
                "serverStatus", core.statusSummary(),
                "hostname", resolveHostname(),
                "version", resolveVersion()
        ));
        return update;
    }

    public void publishLoggedOutState(String retryAtText) {
        runtimeState = "logged_out";
        WebSocket current = webSocket;
        if (!registered || current == null || !connectionOpen) {
            System.err.println("[WS][LOGGED_OUT][UNAVAILABLE] Cannot notify conductor; instanceId=" + instanceId);
            return;
        }
        sendWebSocketMessage(current, buildLoggedOutEvent(retryAtText));
    }

    public void publishStartingState() {
        runtimeState = "starting";
        WebSocket current = webSocket;
        if (!registered || current == null || !connectionOpen) {
            System.err.println("[WS][STARTING][UNAVAILABLE] Cannot notify conductor; instanceId=" + instanceId);
            return;
        }
        sendWebSocketMessage(current, buildStateEvent("starting", "Session restart is starting"));
    }

    public void publishRunningState() {
        runtimeState = "running";
        WebSocket current = webSocket;
        if (!registered || current == null || !connectionOpen) {
            return;
        }
        sendWebSocketMessage(current, buildStateEvent("running", "Session resumed"));
    }

    CommandMessage buildLoggedOutEvent(String retryAtText) {
        CommandMessage message = new CommandMessage();
        message.setId(UUID.randomUUID().toString());
        message.setType("event:logged-out");
        message.setFrom(instanceId);
        message.setTo("conductor");
        message.setTimestamp(System.currentTimeMillis());
        message.setStatus(200);
        message.setPayload(Map.of(
                "state", "logged_out",
                "retryAt", retryAtText,
                "reason", "returned_to_lobby"
        ));
        return message;
    }

    private CommandMessage buildStateEvent(String state, String message) {
        CommandMessage event = new CommandMessage();
        event.setId(UUID.randomUUID().toString());
        event.setType("event:state-changed");
        event.setFrom(instanceId);
        event.setTo("conductor");
        event.setTimestamp(System.currentTimeMillis());
        event.setStatus(200);
        event.setPayload(Map.of(
                "state", state,
                "message", message,
                "instanceId", instanceId
        ));
        return event;
    }

    CommandMessage buildStatusDetailsResponse(CommandMessage incoming, StatusDetails details) {
        CommandMessage response = new CommandMessage();
        response.setId(incoming.getId());
        response.setType("response:status:details");
        response.setFrom(instanceId);
        response.setTo(resolveTarget(incoming));
        response.setTimestamp(System.currentTimeMillis());
        response.setStatus(200);
        response.setPayload(Map.of("instanceId", instanceId, "details", details));
        return response;
    }

    CommandMessage buildThreadConfigurationResponse(
            CommandMessage incoming,
            java.util.List<ThreadDefinition> definitions
    ) {
        return buildThreadConfigurationResponse(incoming, definitions, java.util.List.of());
    }

    CommandMessage buildThreadConfigurationResponse(
            CommandMessage incoming,
            java.util.List<ThreadDefinition> definitions,
            java.util.List<ModuleDefinition> modules
    ) {
        CommandMessage response = new CommandMessage();
        response.setId(incoming.getId());
        response.setType("response:thread:configurations");
        response.setFrom(instanceId);
        response.setTo(resolveTarget(incoming));
        response.setTimestamp(System.currentTimeMillis());
        response.setStatus(200);
        response.setPayload(Map.of(
                "instanceId", instanceId,
                "definitions", definitions,
                "modules", modules,
                "availableDefinitions", ThreadManager.availableDefinitions()
        ));
        return response;
    }

    CommandMessage buildManagedThreadConfigurationResponse(CommandMessage incoming) {
        try {
            if (threadManagerProvider == null) {
                throw new IllegalStateException("Thread manager is unavailable");
            }
            java.util.List<ThreadDefinition> definitions = threadManagerProvider.getObject().managedDefinitions();
            java.util.List<ModuleDefinition> modules = configServiceProvider == null
                    ? java.util.List.of()
                    : configServiceProvider.getObject().listModuleDefinitions();
            CommandMessage response = buildThreadConfigurationResponse(incoming, definitions, modules);
            if (objectMapper.writeValueAsBytes(response).length > MAX_MESSAGE_BYTES) {
                return buildThreadConfigurationError(incoming, 500,
                        "Managed thread configuration response exceeds the message size limit.");
            }
            return response;
        } catch (Exception e) {
            System.err.println("[WS][ERROR] Failed to read managed thread configurations: " + e.getMessage());
            return buildThreadConfigurationError(incoming, 500,
                    "Failed to read managed thread configurations.");
        }
    }

    CommandMessage buildConfigSetResponse(CommandMessage incoming) {
        try {
            if (configServiceProvider == null) {
                throw new IllegalStateException("Configuration service is unavailable");
            }
            JsonNode payload = objectMapper.valueToTree(incoming.getPayload());
            if (!payload.isArray() || payload.size() == 0) {
                throw new IllegalArgumentException("Expected a non-empty configuration list");
            }

            java.util.List<ConfigEntry> entries = new java.util.ArrayList<>();
            for (JsonNode config : payload) {
                if (!config.isObject()
                        || !config.path("module").isTextual()
                        || config.path("module").asText().isBlank()
                        || !config.path("thread").isTextual()
                        || config.path("thread").asText().isBlank()
                        || !config.path("key").isTextual()
                        || config.path("key").asText().isBlank()
                        || !config.path("value").isTextual()) {
                    throw new IllegalArgumentException("Configuration entries require module, thread, key, and value");
                }

                String module = config.path("module").asText();
                String thread = config.path("thread").asText();
                String key = config.path("key").asText();
                String value = config.path("value").asText();
                ConfigEntry entry = new ConfigEntry();
                entry.setModule(module);
                entry.setThread(thread);
                entry.setKey(key);
                entry.setValue(value);
                entries.add(entry);
            }

            configServiceProvider.getObject().updateThreads(entries);
            return buildConfigSetResponse(incoming, entries);
        } catch (IllegalArgumentException e) {
            return buildConfigSetError(incoming, 400, "Failed to update configuration.");
        } catch (RuntimeException e) {
            return buildConfigSetError(incoming, 500, "Failed to update configuration.");
        }
    }

    private CommandMessage buildConfigSetResponse(CommandMessage incoming, java.util.List<ConfigEntry> entries) {
        CommandMessage response = new CommandMessage();
        response.setId(incoming.getId());
        response.setType("response:config:set");
        response.setFrom(instanceId);
        response.setTo(resolveTarget(incoming));
        response.setTimestamp(System.currentTimeMillis());
        response.setStatus(200);
        java.util.List<ConfigDefinition> definitions = entries.stream()
                .map(entry -> new ConfigDefinition(
                        entry.getModule(), entry.getThread(), entry.getKey(), entry.getValue(),
                        String.class, false, ""))
                .toList();
        response.setPayload(definitions);
        return response;
    }

    CommandMessage buildConfigMutationResponse(CommandMessage incoming) {
        try {
            if (configServiceProvider == null) {
                throw new IllegalStateException("Configuration service is unavailable");
            }

            if ("request:module:set".equals(incoming.getType())) {
                ConfigEntry config = objectMapper.convertValue(incoming.getPayload(), ConfigEntry.class);
                if (config == null || config.getModule() == null || config.getModule().isBlank()
                        || config.getKey() == null || config.getKey().isBlank()
                        || config.getValue() == null) {
                    throw new IllegalArgumentException("Module, key, and value are required");
                }
                ModuleDefinition definition = configServiceProvider.getObject().updateModule(config);
                return buildConfigMutationResponse(incoming, 200, null,
                        java.util.Map.of("module", definition));
            }

            if ("request:thread:change-module".equals(incoming.getType())) {
                JsonNode payload = objectMapper.valueToTree(incoming.getPayload());
                ConfigEntry config = objectMapper.convertValue(payload.path("config"), ConfigEntry.class);
                String moduleName = payload.path("module").asText("");
                if (config == null || moduleName.isBlank()) {
                    throw new IllegalArgumentException("Config and destination module are required");
                }
                configServiceProvider.getObject().changeThreadModule(config, moduleName);
                return buildConfigMutationResponse(incoming, 200, null);
            }

            JsonNode payload = objectMapper.valueToTree(incoming.getPayload());
            if ("request:config:delete".equals(incoming.getType()) && payload.isArray()) {
                for (JsonNode node : payload) {
                    ConfigEntry entry = objectMapper.convertValue(node, ConfigEntry.class);
                    if (entry != null) {
                        configServiceProvider.getObject().deleteConfig(entry);
                    }
                }
                return buildConfigMutationResponse(incoming, 200, null);
            }

            ConfigEntry config = objectMapper.convertValue(incoming.getPayload(), ConfigEntry.class);
            if (config == null || config.getModule() == null || config.getModule().isBlank()
                    || config.getThread() == null || config.getThread().isBlank()) {
                throw new IllegalArgumentException("Module and thread are required");
            }
            if ("request:module:delete".equals(incoming.getType())) {
                configServiceProvider.getObject().deleteModule(config);
            } else if ("request:thread:delete".equals(incoming.getType())) {
                configServiceProvider.getObject().deleteThread(config);
            } else if ("request:config:delete".equals(incoming.getType())) {
                configServiceProvider.getObject().deleteConfig(config);
            } else {
                throw new IllegalArgumentException("Unsupported configuration mutation");
            }
            return buildConfigMutationResponse(incoming, 200, null);
        } catch (IllegalArgumentException e) {
            return buildConfigMutationResponse(incoming, 400, e.getMessage());
        } catch (RuntimeException e) {
            return buildConfigMutationResponse(incoming, 500, "Failed to mutate configuration.");
        }
    }

    private CommandMessage buildConfigMutationResponse(CommandMessage incoming, int status,
                                                       String errorMessage, Object payload) {
        CommandMessage response = new CommandMessage();
        response.setId(incoming.getId());
        response.setType(status == 200
                ? "response:" + incoming.getType().substring("request:".length())
                : "error:" + incoming.getType().substring("request:".length()));
        response.setFrom(instanceId);
        response.setTo(resolveTarget(incoming));
        response.setTimestamp(System.currentTimeMillis());
        response.setStatus(status);
        response.setPayload(payload);
        response.setError(errorMessage);
        return response;
    }

    private CommandMessage buildConfigMutationResponse(CommandMessage incoming, int status, String errorMessage) {
        CommandMessage response = new CommandMessage();
        response.setId(incoming.getId());
        response.setType(status == 200
                ? "response:" + incoming.getType().substring("request:".length())
                : "error:" + incoming.getType().substring("request:".length()));
        response.setFrom(instanceId);
        response.setTo(resolveTarget(incoming));
        response.setTimestamp(System.currentTimeMillis());
        response.setStatus(status);
        if (status == 200 && "request:config:delete".equals(incoming.getType())) {
            response.setPayload(configServiceProvider.getObject().listConfigs());
        } else {
            response.setPayload(status == 200 ? java.util.List.of() : null);
        }
        response.setError(errorMessage);
        return response;
    }

    private CommandMessage buildConfigSetError(CommandMessage incoming, int status, String errorMessage) {
        CommandMessage error = new CommandMessage();
        error.setId(incoming.getId());
        error.setType("error:config:set");
        error.setFrom(instanceId);
        error.setTo(resolveTarget(incoming));
        error.setTimestamp(System.currentTimeMillis());
        error.setStatus(status);
        error.setError(errorMessage);
        return error;
    }

    private CommandMessage buildThreadConfigurationError(CommandMessage incoming, int status, String error) {
        CommandMessage response = new CommandMessage();
        response.setId(incoming.getId());
        response.setType("error:thread:configurations");
        response.setFrom(instanceId);
        response.setTo(resolveTarget(incoming));
        response.setTimestamp(System.currentTimeMillis());
        response.setStatus(status);
        response.setError(error);
        return response;
    }

    CommandMessage buildCommandCatalogResponse(CommandMessage incoming, CommandCatalogResponse catalog) {
        CommandMessage response = new CommandMessage();
        response.setId(incoming.getId());
        response.setType("response:command:catalog");
        response.setFrom(instanceId);
        response.setTo(resolveTarget(incoming));
        response.setTimestamp(System.currentTimeMillis());
        response.setStatus(200);
        response.setPayload(catalog == null ? new CommandCatalogResponse() : catalog);
        return response;
    }

    CommandMessage buildCommandSubmitResponse(CommandMessage incoming, CommandSubmitResponse result) {
        CommandMessage response = new CommandMessage();
        response.setId(incoming.getId());
        response.setType("response:command:submit");
        response.setFrom(instanceId);
        response.setTo(resolveTarget(incoming));
        response.setTimestamp(System.currentTimeMillis());
        response.setStatus(200);
        response.setPayload(result == null ? CommandSubmitResponse.builder().message("No result returned").build() : result);
        return response;
    }

    String toResponseType(String incomingType) {
        if (incomingType == null || incomingType.isBlank()) {
            return "response:unknown";
        }
        if (incomingType.startsWith("request:")) {
            return "response:" + incomingType.substring("request:".length());
        }
        if (incomingType.startsWith("response:") || incomingType.startsWith("error:")) {
            return incomingType;
        }
        return "response:" + incomingType;
    }

    private String resolveTarget(CommandMessage incoming) {
        if (incoming.getFrom() != null && !incoming.getFrom().isBlank()) {
            return incoming.getFrom();
        }
        if (incoming.getTo() != null && !incoming.getTo().isBlank() && !instanceId.equals(incoming.getTo())) {
            return incoming.getTo();
        }
        return "conductor";
    }

    private void sendWebSocketMessage(WebSocket socket, CommandMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            int messageBytes = json.getBytes(StandardCharsets.UTF_8).length;
            if (messageBytes > MAX_MESSAGE_BYTES) {
                System.err.println("[WS][ERROR] Refusing oversized outgoing message type=" + message.getType()
                        + " bytes=" + messageBytes + " limit=" + MAX_MESSAGE_BYTES);
                return;
            }
            if (!socket.send(json)) {
                System.err.println("[WS][ERROR] WebSocket rejected outgoing message type=" + message.getType()
                        + " id=" + message.getId());
            }
        } catch (Exception e) {
            System.err.println("[WS][ERROR] Failed to send message: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    @Scheduled(fixedDelay = 10_000)
    public void publishStatusUpdate() {
        WebSocket current = webSocket;
        if (registered && current != null) {
            long queuedBytes = current.queueSize();
            if (queuedBytes >= MAX_MESSAGE_BYTES) {
                System.err.println("[WS][STATUS][COALESCED] queuedBytes=" + queuedBytes
                        + " instanceId=" + instanceId);
                return;
            }
            sendWebSocketMessage(current, buildStatusUpdate());
        }
    }

    public void publishRestartRequired(String reason) {
        WebSocket current = webSocket;
        if (!registered || current == null || !connectionOpen) {
            System.err.println("[WS][RESTART][UNAVAILABLE] Cannot notify conductor; instanceId=" + instanceId);
            return;
        }
        sendWebSocketMessage(current, buildRestartRequiredEvent(reason));
    }

    CommandMessage buildRestartRequiredEvent(String reason) {
        CommandMessage message = new CommandMessage();
        message.setId(UUID.randomUUID().toString());
        message.setType("event:restart-required");
        message.setFrom(instanceId);
        message.setTo("conductor");
        message.setTimestamp(System.currentTimeMillis());
        message.setStatus(200);
        message.setPayload(Map.of("reason", reason));
        return message;
    }

    @Scheduled(fixedDelay = 5_000)
    public void reconnectIfDisconnected() {
        if (!connectionOpen && !connecting && !shuttingDown && !managerUrl.isBlank()) {
            connect();
        }
    }

    private String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-host";
        }
    }

    private String resolveVersion() {
        Package currentPackage = getClass().getPackage();
        if (currentPackage != null) {
            String version = currentPackage.getImplementationVersion();
            if (version != null && !version.isBlank()) {
                return version;
            }
        }
        return "dev";
    }

    private CommandMessage buildParseError(String rawPayload, Exception cause) {
        CommandMessage error = new CommandMessage();
        error.setId(UUID.randomUUID().toString());
        error.setType("error:parse");
        error.setFrom(instanceId);
        error.setTo("conductor");
        error.setTimestamp(System.currentTimeMillis());
        error.setStatus(400);
        error.setError("Invalid message format: " + cause.getMessage());
        error.setPayload(rawPayload);
        error.setMetadata(new LinkedHashMap<>(Map.of("errorCode", "INVALID_FORMAT")));
        return error;
    }

    private final class WorkerListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            connectionOpen = true;
            connecting = false;
            System.out.println("[WS][OPEN] managerUrl=" + managerUrl + " instanceId=" + instanceId);
            CommandMessage registration = buildRegistrationMessage();
            registrationRequestId = registration.getId();
            sendWebSocketMessage(webSocket, registration);
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            try {
                CommandMessage incoming = objectMapper.readValue(text, CommandMessage.class);
                if ("response:register".equals(incoming.getType())
                        && registrationRequestId != null
                        && registrationRequestId.equals(incoming.getId())
                        && incoming.getStatus() == 200) {
                    registered = true;
                    System.out.println("[WS][REGISTERED] instanceId=" + instanceId);
                    sendWebSocketMessage(webSocket, buildStatusUpdate());
                } else if ("error:register".equals(incoming.getType())
                        && registrationRequestId != null
                        && registrationRequestId.equals(incoming.getId())) {
                    registered = false;
                    System.err.println("[WS][REGISTRATION][ERROR] instanceId=" + instanceId
                            + " message=" + incoming.getError());
                } else if ("response:register".equals(incoming.getType())
                        || "error:register".equals(incoming.getType())) {
                    System.err.println("[WS][REGISTRATION][ERROR] Ignoring uncorrelated registration response");
                } else if (isRequest(incoming)
                        && (!"conductor".equals(incoming.getFrom()) || !instanceId.equals(incoming.getTo()))) {
                    System.err.println("[WS][REJECTED] Request is not addressed from conductor to " + instanceId);
                } else if ("request:status".equals(incoming.getType()) && registered) {
                    sendWebSocketMessage(webSocket, buildStatusResponse(incoming));
                } else if ("request:stop".equals(incoming.getType()) && registered) {
                    System.out.println("[WS][STOP] Received stop request id=" + incoming.getId());
                    sendWebSocketMessage(webSocket, buildStopResponse(incoming));
                    if (workerLifecycleProvider == null) {
                        System.err.println("[WS][ERROR] Cannot stop instance: lifecycle is unavailable");
                    } else {
                        Thread stopThread = new Thread(
                                () -> {
                                    System.out.println("[WS][STOP] Starting graceful shutdown thread");
                                    try {
                                        workerLifecycleProvider.getObject().stop();
                                    } catch (RuntimeException e) {
                                        System.err.println("[WS][STOP] Graceful shutdown thread failed: " + e.getMessage());
                                        e.printStackTrace(System.err);
                                    }
                                },
                                "snark-conductor-stop");
                        stopThread.start();
                    }
                } else if ("request:status:details".equals(incoming.getType()) && registered) {
                    JsonNode detailsRequest = objectMapper.valueToTree(incoming.getPayload());
                    int offset = detailsRequest.path("offset").asInt(-1);
                    int limit = detailsRequest.path("limit").asInt(-1);
                    if (offset < 0 || limit < 1 || limit > MAX_STATUS_DETAIL_PAGE_SIZE) {
                        sendWebSocketMessage(webSocket, buildRequestError(incoming,
                                400, "Status details require offset >= 0 and limit between 1 and 25."));
                    } else {
                        try {
                            sendWebSocketMessage(webSocket, buildStatusDetailsResponse(
                                    incoming, core.statusDetails(offset, limit)));
                        } catch (RuntimeException e) {
                            System.err.println("[WS][ERROR] Failed to create status details page: " + e.getMessage());
                            sendWebSocketMessage(webSocket, buildRequestError(incoming, 500,
                                    "Failed to create status details page."));
                        }
                    }
                } else if ("request:thread:configurations".equals(incoming.getType()) && registered) {
                    sendWebSocketMessage(webSocket, buildManagedThreadConfigurationResponse(incoming));
                } else if ("request:config:set".equals(incoming.getType()) && registered) {
                    sendWebSocketMessage(webSocket, buildConfigSetResponse(incoming));
                } else if (("request:config:delete".equals(incoming.getType())
                        || "request:thread:change-module".equals(incoming.getType())
                        || "request:module:set".equals(incoming.getType())
                        || "request:module:delete".equals(incoming.getType())
                        || "request:thread:delete".equals(incoming.getType())) && registered) {
                    sendWebSocketMessage(webSocket, buildConfigMutationResponse(incoming));
                } else if ("request:command:catalog".equals(incoming.getType()) && registered && commandRecipeService != null) {
                    sendWebSocketMessage(webSocket, buildCommandCatalogResponse(incoming, commandRecipeService.listCommands()));
                } else if ("request:command:submit".equals(incoming.getType()) && registered && commandRecipeService != null) {
                    CommandSubmitRequest request = objectMapper.convertValue(incoming.getPayload(), CommandSubmitRequest.class);
                    sendWebSocketMessage(webSocket, buildCommandSubmitResponse(incoming, commandRecipeService.submitRecipe(request)));
                } else if (isRequest(incoming) && registered) {
                    sendWebSocketMessage(webSocket, buildEchoResponse(incoming));
                } else if (isRequest(incoming)) {
                    System.err.println("[WS][REJECTED] Request received before registration acknowledgement");
                } else {
                    System.out.println("[WS][IGNORED] Non-request message type=" + incoming.getType());
                }
            } catch (Exception e) {
                System.err.println("[WS][ERROR] Failed to parse incoming message: " + e.getMessage());
                e.printStackTrace(System.err);
                sendWebSocketMessage(webSocket, buildParseError(text, e));
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            registered = false;
            System.out.println("[WS][CLOSING] code=" + code + " reason=" + reason);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            registered = false;
            if (SnarkWebSocketClient.this.webSocket == webSocket) {
                connectionOpen = false;
                connecting = false;
            }
            System.out.println("[WS][CLOSED] code=" + code + " reason=" + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            registered = false;
            if (SnarkWebSocketClient.this.webSocket == webSocket) {
                connectionOpen = false;
                connecting = false;
            }
            System.err.println("[WS][FAILURE] managerUrl=" + managerUrl + " instanceId=" + instanceId
                    + " message=" + t.getMessage());
            if (response != null) {
                System.err.println("[WS][FAILURE] httpStatus=" + response.code());
            }
            t.printStackTrace(System.err);
        }
    }

    private boolean isRequest(CommandMessage message) {
        return message.getType() != null && message.getType().startsWith("request:");
    }

    private CommandMessage buildRequestError(CommandMessage incoming, int status, String errorMessage) {
        CommandMessage error = new CommandMessage();
        error.setId(incoming.getId());
        error.setType("error:status:details");
        error.setFrom(instanceId);
        error.setTo(resolveTarget(incoming));
        error.setTimestamp(System.currentTimeMillis());
        error.setStatus(status);
        error.setError(errorMessage);
        return error;
    }
}
