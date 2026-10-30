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
import org.enoch.common.command.CommandMessage;
import org.enoch.common.status.StatusDetails;
import org.enoch.snark.instance.si.Core;
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
    private final AtomicLong statusRevision = new AtomicLong();

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
        this.managerUrl = managerUrl == null ? "" : managerUrl.trim();
        this.instanceId = instanceId == null || instanceId.isBlank() ? "snark" : instanceId.trim();
        this.core = core;
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
        sendAndLog(current, buildLoggedOutEvent(retryAtText));
    }

    public void publishStartingState() {
        runtimeState = "starting";
        WebSocket current = webSocket;
        if (!registered || current == null || !connectionOpen) {
            System.err.println("[WS][STARTING][UNAVAILABLE] Cannot notify conductor; instanceId=" + instanceId);
            return;
        }
        sendAndLog(current, buildStateEvent("starting", "Session restart is starting"));
    }

    public void publishRunningState() {
        runtimeState = "running";
        WebSocket current = webSocket;
        if (!registered || current == null || !connectionOpen) {
            return;
        }
        sendAndLog(current, buildStateEvent("running", "Session resumed"));
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

    private void sendAndLog(WebSocket socket, CommandMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            int messageBytes = json.getBytes(StandardCharsets.UTF_8).length;
            if (messageBytes > MAX_MESSAGE_BYTES) {
                System.err.println("[WS][ERROR] Refusing oversized outgoing message type=" + message.getType()
                        + " bytes=" + messageBytes + " limit=" + MAX_MESSAGE_BYTES);
                return;
            }
            System.out.println("[WS][OUT] " + json);
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
            sendAndLog(current, buildStatusUpdate());
        }
    }

    public void publishRestartRequired(String reason) {
        WebSocket current = webSocket;
        if (!registered || current == null || !connectionOpen) {
            System.err.println("[WS][RESTART][UNAVAILABLE] Cannot notify conductor; instanceId=" + instanceId);
            return;
        }
        sendAndLog(current, buildRestartRequiredEvent(reason));
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
            sendAndLog(webSocket, registration);
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            System.out.println("[WS][IN] " + text);
            try {
                CommandMessage incoming = objectMapper.readValue(text, CommandMessage.class);
                if ("response:register".equals(incoming.getType())
                        && registrationRequestId != null
                        && registrationRequestId.equals(incoming.getId())
                        && incoming.getStatus() == 200) {
                    registered = true;
                    System.out.println("[WS][REGISTERED] instanceId=" + instanceId);
                    sendAndLog(webSocket, buildStatusUpdate());
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
                    sendAndLog(webSocket, buildStatusResponse(incoming));
                } else if ("request:status:details".equals(incoming.getType()) && registered) {
                    JsonNode detailsRequest = objectMapper.valueToTree(incoming.getPayload());
                    int offset = detailsRequest.path("offset").asInt(-1);
                    int limit = detailsRequest.path("limit").asInt(-1);
                    if (offset < 0 || limit < 1 || limit > MAX_STATUS_DETAIL_PAGE_SIZE) {
                        sendAndLog(webSocket, buildRequestError(incoming,
                                400, "Status details require offset >= 0 and limit between 1 and 25."));
                    } else {
                        try {
                            sendAndLog(webSocket, buildStatusDetailsResponse(
                                    incoming, core.statusDetails(offset, limit)));
                        } catch (RuntimeException e) {
                            System.err.println("[WS][ERROR] Failed to create status details page: " + e.getMessage());
                            sendAndLog(webSocket, buildRequestError(incoming, 500,
                                    "Failed to create status details page."));
                        }
                    }
                } else if (isRequest(incoming) && registered) {
                    sendAndLog(webSocket, buildEchoResponse(incoming));
                } else if (isRequest(incoming)) {
                    System.err.println("[WS][REJECTED] Request received before registration acknowledgement");
                } else {
                    System.out.println("[WS][IGNORED] Non-request message type=" + incoming.getType());
                }
            } catch (Exception e) {
                System.err.println("[WS][ERROR] Failed to parse incoming message: " + e.getMessage());
                e.printStackTrace(System.err);
                sendAndLog(webSocket, buildParseError(text, e));
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
