package org.enoch.conductor.ws;

import org.enoch.common.command.CommandMessage;
import org.enoch.common.status.NavigatorStatus;
import org.enoch.common.status.ServerStatus;
import org.enoch.common.status.StatusDetails;
import org.enoch.conductor.config.InstanceConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

@Component
public class WorkerSocketHandler extends TextWebSocketHandler {

    private static final long STATUS_REQUEST_TIMEOUT_MILLIS = 60_000L;
    private static final int MAX_MESSAGE_BYTES = 16 * 1024;
    private static final int MAX_STATUS_DETAIL_PAGE_SIZE = 25;

    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final Map<String, WebSocketSession> workers = new ConcurrentHashMap<>();
    private final Map<String, Long> lastRegisteredAt = new ConcurrentHashMap<>();
    private final Map<String, ServerStatus> lastStatuses = new ConcurrentHashMap<>();
    private final Map<String, Long> lastStatusUpdatedAt = new ConcurrentHashMap<>();
    private final Map<String, Long> lastStatusRevisions = new ConcurrentHashMap<>();
    private final Map<String, PendingStatusRequest> pendingStatusRequests = new ConcurrentHashMap<>();
    private final Map<String, PendingStatusDetailsRequest> pendingStatusDetailsRequests = new ConcurrentHashMap<>();
    
    private InstanceConfigService instanceConfigService;
    private volatile BiConsumer<String, String> restartRequestHandler = (instanceId, reason) -> {
        System.err.println("[WS][WORKER][RESTART][UNHANDLED] instanceId=" + instanceId
                + " reason=" + reason);
    };

    public void setInstanceConfigService(InstanceConfigService service) {
        this.instanceConfigService = service;
    }

    public void setRestartRequestHandler(BiConsumer<String, String> handler) {
        this.restartRequestHandler = handler;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("[WS][WORKER][CONNECTED] sessionId=" + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        int messageBytes = message.getPayload().getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        if (messageBytes > MAX_MESSAGE_BYTES) {
            System.err.println("[WS][WORKER][REJECTED] Oversized message sessionId=" + session.getId()
                    + " bytes=" + messageBytes + " limit=" + MAX_MESSAGE_BYTES);
            session.close(CloseStatus.TOO_BIG_TO_PROCESS);
            return;
        }
        logIncoming(session, message.getPayload());

        JsonNode json = mapper.readTree(message.getPayload());

        String type = json.path("type").asText("");

        if ("request:register".equals(type)) {
            registerWorker(session, json);
            return;
        }

        String registeredInstanceId = findInstanceIdBySession(session);
        if (registeredInstanceId == null) {
            System.err.println("[WS][WORKER][REJECTED] message from unregistered sessionId=" + session.getId()
                    + " type=" + type);
            return;
        }

        if ("update:status".equals(type)) {
            storeStatusUpdate(registeredInstanceId, session, json);
        } else if ("event:restart-required".equals(type)) {
            handleRestartRequired(registeredInstanceId, session, json);
        } else if ("event:logged-out".equals(type)) {
            handleLoggedOut(registeredInstanceId, session, json);
        } else if ("event:state-changed".equals(type)) {
            handleStateChanged(registeredInstanceId, session, json);
        } else if (isStatusMessage(type)) {
            storeStatusResponse(registeredInstanceId, session, json);
        } else if ("response:status:details".equals(type) || "error:status:details".equals(type)) {
            storeStatusDetailsResponse(registeredInstanceId, session, json);
        }

        if (isConfigResponse(type) && instanceConfigService != null) {
            CommandMessage response = mapper.treeToValue(json, CommandMessage.class);
            instanceConfigService.handleConfigResponse(response);
        }

        if (type.equals("LOG")) {
            System.out.println("[WORKER] " + json.path("message").asText(""));
        }
    }

    private void handleRestartRequired(String instanceId, WebSocketSession session, JsonNode json) {
        JsonNode payload = json.path("payload");
        if (workers.get(instanceId) != session
                || json.path("id").asText("").isBlank()
                || !instanceId.equals(json.path("from").asText(""))
                || !"conductor".equals(json.path("to").asText(""))
                || json.path("status").asInt() != 200
                || !payload.isObject()
                || !payload.path("reason").isTextual()
                || payload.path("reason").asText().isBlank()) {
            System.err.println("[WS][WORKER][REJECTED] Malformed restart-required event instanceId=" + instanceId);
            return;
        }

        String reason = payload.path("reason").asText();
        System.err.println("[WS][WORKER][RESTART][REQUESTED] instanceId=" + instanceId + " reason=" + reason);
        restartRequestHandler.accept(instanceId, reason);
    }

    private void handleLoggedOut(String instanceId, WebSocketSession session, JsonNode json) {
        JsonNode payload = json.path("payload");
        if (workers.get(instanceId) != session
                || json.path("id").asText("").isBlank()
                || !instanceId.equals(json.path("from").asText(""))
                || !"conductor".equals(json.path("to").asText(""))
                || json.path("status").asInt() != 200
                || !payload.isObject()
                || !"logged_out".equals(payload.path("state").asText(""))
                || !payload.path("retryAt").isTextual()
                || payload.path("retryAt").asText().isBlank()) {
            System.err.println("[WS][WORKER][REJECTED] Malformed logged-out event instanceId=" + instanceId);
            return;
        }

        String retryAt = payload.path("retryAt").asText();
        System.err.println("[WS][WORKER][LOGGED_OUT] instanceId=" + instanceId + " retryAt=" + retryAt);
        lastStatuses.put(instanceId, new org.enoch.common.status.ServerStatus(
                new org.enoch.common.status.NavigatorStatus("Waiting for next attempt at " + retryAt, null),
                List.of(),
                "",
                "",
                null));
        lastStatusUpdatedAt.put(instanceId, System.currentTimeMillis());
    }

    private void handleStateChanged(String instanceId, WebSocketSession session, JsonNode json) {
        JsonNode payload = json.path("payload");
        if (workers.get(instanceId) != session
                || json.path("id").asText("").isBlank()
                || !instanceId.equals(json.path("from").asText(""))
                || !"conductor".equals(json.path("to").asText(""))
                || json.path("status").asInt() != 200
                || !payload.isObject()
                || payload.path("state").asText("").isBlank()) {
            System.err.println("[WS][WORKER][REJECTED] Malformed state-changed event instanceId=" + instanceId);
            return;
        }

        String state = payload.path("state").asText();
        String message = payload.path("message").asText("");
        if ("starting".equals(state) || "running".equals(state)) {
            System.err.println("[WS][WORKER][STATE] instanceId=" + instanceId + " state=" + state + " message=" + message);
            lastStatusUpdatedAt.put(instanceId, System.currentTimeMillis());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String instanceId = findInstanceIdBySession(session);
        System.out.println("[WS][WORKER][DISCONNECTED] sessionId=" + session.getId()
                + " instanceId=" + (instanceId != null ? instanceId : "<unknown>")
                + " status=" + status);

        if (instanceId != null) {
            workers.remove(instanceId, session);
            lastStatuses.remove(instanceId);
            lastStatusUpdatedAt.remove(instanceId);
            lastStatusRevisions.remove(instanceId);
            pendingStatusRequests.entrySet().removeIf(entry -> instanceId.equals(entry.getValue().instanceId()));
            pendingStatusDetailsRequests.entrySet().removeIf(entry -> {
                if (instanceId.equals(entry.getValue().instanceId())) {
                    entry.getValue().result().completeExceptionally(
                            new IllegalStateException("Instance disconnected before status details arrived"));
                    return true;
                }
                return false;
            });
            if (instanceConfigService != null) {
                instanceConfigService.clearInstanceState(instanceId);
                instanceConfigService.cancelPendingRequests(instanceId);
            }
        }
    }

    private void registerWorker(WebSocketSession session, JsonNode json) throws Exception {
        String requestId = json.path("id").asText("");
        String instanceId = json.path("payload").path("instanceId").asText("");
        String sender = json.path("from").asText("");
        if (requestId.isBlank()) {
            System.err.println("[WS][WORKER][REJECTED] Registration is missing message id sessionId=" + session.getId());
            session.close(CloseStatus.PROTOCOL_ERROR);
            return;
        }
        if (json.path("status").asInt() != 100
                || !"conductor".equals(json.path("to").asText(""))
                || instanceId.isBlank() || !instanceId.equals(sender)) {
            sendRegistrationResponse(session, requestId, sender, sender,
                    400, "Registration requires status=100, to=conductor, and matching from and payload.instanceId values.");
            return;
        }

        WebSocketSession previousSession = workers.put(instanceId, session);
        lastRegisteredAt.put(instanceId, System.currentTimeMillis());
        lastStatuses.remove(instanceId);
        lastStatusUpdatedAt.remove(instanceId);
        lastStatusRevisions.remove(instanceId);
        pendingStatusRequests.entrySet().removeIf(entry -> instanceId.equals(entry.getValue().instanceId()));
        pendingStatusDetailsRequests.entrySet().removeIf(entry -> {
            if (instanceId.equals(entry.getValue().instanceId())) {
                entry.getValue().result().completeExceptionally(
                        new IllegalStateException("Instance session was replaced before status details arrived"));
                return true;
            }
            return false;
        });
        if (previousSession != null && previousSession != session) {
            try {
                previousSession.close(CloseStatus.NORMAL);
            } catch (IOException e) {
                System.err.println("[WS][WORKER][ERROR] Failed to close replaced session for instanceId="
                        + instanceId + ": " + e.getMessage());
            }
            if (instanceConfigService != null) {
                instanceConfigService.clearInstanceState(instanceId);
                instanceConfigService.cancelPendingRequests(instanceId);
            }
        }
        sendRegistrationResponse(session, requestId, sender, instanceId, 200, null);
        System.out.println("[WS][WORKER][REGISTERED] instanceId=" + instanceId + " sessionId=" + session.getId());
    }

    private void sendRegistrationResponse(WebSocketSession session, String requestId, String sender,
                                          String instanceId, int status, String error) throws Exception {
        CommandMessage response = new CommandMessage();
        response.setId(requestId);
        response.setType(status == 200 ? "response:register" : "error:register");
        response.setFrom("conductor");
        response.setTo(instanceId.isBlank() ? sender : instanceId);
        response.setTimestamp(System.currentTimeMillis());
        response.setStatus(status);
        response.setPayload(status == 200 ? Map.of("registered", true) : null);
        response.setError(error);
        String payload = mapper.writeValueAsString(response);
        logOutgoing(session, instanceId, payload);
        session.sendMessage(new TextMessage(payload));
    }

    private String findInstanceIdBySession(WebSocketSession session) {
        for (Map.Entry<String, WebSocketSession> entry : workers.entrySet()) {
            if (entry.getValue().equals(session)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean requestStatus(String workerId) throws Exception {

        WebSocketSession session = workers.get(workerId);

        if (session == null || !session.isOpen()) {
            return false;
        }

        removeExpiredStatusRequests();
        CommandMessage msg = new CommandMessage();
        msg.setId(UUID.randomUUID().toString());
        msg.setType("request:status");
        msg.setFrom("conductor");
        msg.setTo(workerId);
        msg.setTimestamp(System.currentTimeMillis());
        msg.setStatus(100);

        String json = mapper.writeValueAsString(msg);
        pendingStatusRequests.put(msg.getId(),
                new PendingStatusRequest(workerId, session.getId(), System.currentTimeMillis()));
        logOutgoing(session, workerId, json);
        try {
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            pendingStatusRequests.remove(msg.getId());
            throw e;
        }
        return true;
    }

    public StatusDetails requestStatusDetails(String workerId, int offset, int limit) throws Exception {
        if (offset < 0 || limit < 1 || limit > MAX_STATUS_DETAIL_PAGE_SIZE) {
            throw new IllegalArgumentException("Status details require offset >= 0 and limit between 1 and 25");
        }
        WebSocketSession session = workers.get(workerId);
        if (session == null || !session.isOpen()) {
            throw new IllegalStateException("Instance is offline");
        }

        CommandMessage request = new CommandMessage();
        request.setId(UUID.randomUUID().toString());
        request.setType("request:status:details");
        request.setFrom("conductor");
        request.setTo(workerId);
        request.setTimestamp(System.currentTimeMillis());
        request.setStatus(100);
        request.setPayload(Map.of("offset", offset, "limit", limit));
        CompletableFuture<StatusDetails> result = new CompletableFuture<>();
        pendingStatusDetailsRequests.put(request.getId(),
                new PendingStatusDetailsRequest(workerId, session.getId(), offset, limit, result));
        String json = mapper.writeValueAsString(request);
        logOutgoing(session, workerId, json);
        try {
            session.sendMessage(new TextMessage(json));
            return result.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new IllegalStateException("Timed out waiting for status details from instance " + workerId, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } finally {
            pendingStatusDetailsRequests.remove(request.getId());
        }
    }

    public ServerStatus getLastServerStatus(String workerId) {
        return lastStatuses.get(workerId);
    }

    public String getLastStatusMessage(String workerId) {
        ServerStatus status = lastStatuses.get(workerId);
        return status == null || status.getNavigatorStatus() == null
                ? null
                : status.getNavigatorStatus().getFleetStatus();
    }

    public long getLastStatusUpdatedAt(String workerId) {

        return lastStatusUpdatedAt.getOrDefault(workerId, 0L);
    }

    public boolean isRegistered(String workerId) {
        WebSocketSession session = workers.get(workerId);
        return session != null && session.isOpen();
    }

    public long getLastRegisteredAt(String workerId) {
        return lastRegisteredAt.getOrDefault(workerId, 0L);
    }

    public void send(String workerId, String json) throws Exception {

        WebSocketSession session = workers.get(workerId);

        if (session == null || !session.isOpen()) {
            throw new IllegalStateException("Instance is offline");
        }

        logOutgoing(session, workerId, json);
        session.sendMessage(new TextMessage(json));
    }

    public void broadcast(String json) throws Exception {

        for (WebSocketSession session : workers.values()) {
            logOutgoing(session, resolveWorkerId(session), json);
            session.sendMessage(new TextMessage(json));
        }
    }

    private void logIncoming(WebSocketSession session, String payload) {
        System.out.println("[WS][WORKER][IN] sessionId=" + session.getId()
                + " instanceId=" + resolveWorkerId(session)
                + " payload=" + payload);
    }

    private void logOutgoing(WebSocketSession session, String workerId, String payload) {
        System.out.println("[WS][WORKER][OUT] sessionId=" + session.getId()
                + " instanceId=" + (workerId != null ? workerId : resolveWorkerId(session))
                + " payload=" + payload);
    }

    private String resolveWorkerId(WebSocketSession session) {
        String instanceId = findInstanceIdBySession(session);
        return instanceId != null ? instanceId : "<unregistered>";
    }

    private boolean isStatusMessage(String type) {
        return "response:status".equals(type);
    }

    private void storeStatusResponse(String instanceId, WebSocketSession session, JsonNode json) {
        String requestId = json.path("id").asText("");
        PendingStatusRequest pending = pendingStatusRequests.remove(requestId);
        if (pending == null
                || !instanceId.equals(pending.instanceId())
                || !pending.sessionId().equals(session.getId())
                || workers.get(instanceId) != session
                || !instanceId.equals(json.path("from").asText(""))
                || !"conductor".equals(json.path("to").asText(""))
                || json.path("status").asInt() != 200) {
            System.err.println("[WS][WORKER][REJECTED] Unmatched status response instanceId=" + instanceId
                    + " requestId=" + requestId);
            return;
        }

        JsonNode payload = json.path("payload");
        JsonNode serverStatusNode = payload.path("serverStatus");
        if (!payload.isObject() || !instanceId.equals(payload.path("instanceId").asText(""))) {
            System.err.println("[WS][WORKER][REJECTED] Malformed status response instanceId=" + instanceId
                    + " requestId=" + requestId);
            return;
        }

        if (serverStatusNode.isMissingNode() && payload.path("message").isTextual()
                && !payload.path("message").asText().isBlank()) {
            lastStatuses.put(instanceId, new ServerStatus(
                    new NavigatorStatus(payload.path("message").asText(), null),
                    List.of(),
                    "",
                    "",
                    null
            ));
            updateStatusTimestamp(instanceId);
            return;
        }

        if (!isValidServerStatus(serverStatusNode)) {
            List<String> statusFields = new ArrayList<>();
            serverStatusNode.fieldNames().forEachRemaining(statusFields::add);
            System.err.println("[WS][WORKER][REJECTED] Malformed status response instanceId=" + instanceId
                    + " requestId=" + requestId + " serverStatusType=" + serverStatusNode.getNodeType()
                    + " serverStatusFields=" + statusFields);
            return;
        }

        try {
            ServerStatus status = mapper.treeToValue(serverStatusNode, ServerStatus.class);
            long revision = payload.path("revision").asLong(-1);
            if (revision >= 0 && !applyRevision(instanceId, revision)) {
                return;
            }
            lastStatuses.put(instanceId, status);
        } catch (Exception e) {
            System.err.println("[WS][WORKER][REJECTED] Invalid server status instanceId=" + instanceId
                    + " requestId=" + requestId + ": " + e.getMessage());
            return;
        }
        updateStatusTimestamp(instanceId);
    }

    private void storeStatusUpdate(String instanceId, WebSocketSession session, JsonNode json) {
        String messageId = json.path("id").asText("");
        JsonNode payload = json.path("payload");
        JsonNode serverStatusNode = payload.path("serverStatus");
        long revision = payload.path("revision").asLong(-1);
        if (workers.get(instanceId) != session
                || messageId.isBlank()
                || !instanceId.equals(json.path("from").asText(""))
                || !"conductor".equals(json.path("to").asText(""))
                || json.path("status").asInt() != 200
                || !payload.isObject()
                || !instanceId.equals(payload.path("instanceId").asText(""))
                || revision < 0
                || !isValidServerStatus(serverStatusNode)) {
            System.err.println("[WS][WORKER][REJECTED] Malformed or unauthorized status update instanceId="
                    + instanceId + " messageId=" + messageId);
            return;
        }
        try {
            ServerStatus status = mapper.treeToValue(serverStatusNode, ServerStatus.class);
            if (!applyRevision(instanceId, revision)) {
                return;
            }
            lastStatuses.put(instanceId, status);
            updateStatusTimestamp(instanceId);
        } catch (Exception e) {
            System.err.println("[WS][WORKER][REJECTED] Invalid status update instanceId=" + instanceId
                    + " messageId=" + messageId + ": " + e.getMessage());
        }
    }

    private boolean applyRevision(String instanceId, long revision) {
        final boolean[] applied = {false};
        lastStatusRevisions.compute(instanceId, (ignored, previous) -> {
            if (previous == null || revision > previous) {
                applied[0] = true;
                return revision;
            }
            return previous;
        });
        if (!applied[0]) {
            System.out.println("[WS][WORKER][IGNORED] Stale status revision instanceId=" + instanceId
                    + " revision=" + revision);
        }
        return applied[0];
    }

    private void storeStatusDetailsResponse(String instanceId, WebSocketSession session, JsonNode json) {
        String requestId = json.path("id").asText("");
        PendingStatusDetailsRequest pending = pendingStatusDetailsRequests.get(requestId);
        WebSocketSession currentSession = workers.get(instanceId);
        if (pending == null || !instanceId.equals(pending.instanceId())
                || currentSession == null || currentSession != session
                || !pending.sessionId().equals(session.getId())
                || !instanceId.equals(json.path("from").asText(""))
                || !"conductor".equals(json.path("to").asText(""))) {
            System.err.println("[WS][WORKER][REJECTED] Unmatched status details response instanceId="
                    + instanceId + " requestId=" + requestId);
            return;
        }
        if ("error:status:details".equals(json.path("type").asText())) {
            pending.result().completeExceptionally(new IllegalStateException(
                    json.path("error").asText("Worker rejected the status details request")));
            return;
        }
        if (json.path("status").asInt() != 200
                || !instanceId.equals(json.path("payload").path("instanceId").asText(""))) {
            pending.result().completeExceptionally(new IllegalStateException("Malformed status details response"));
            return;
        }
        try {
            StatusDetails details = mapper.treeToValue(
                    json.path("payload").path("details"), StatusDetails.class);
            if (details.offset() < 0 || details.limit() != pending.requestedLimit()
                    || details.offset() != pending.requestedOffset()
                    || details.limit() > MAX_STATUS_DETAIL_PAGE_SIZE
                    || details.total() < 0
                    || details.planetCount() < 0
                    || details.moonCount() < 0
                    || details.colonies() == null
                    || details.colonies().size() > details.limit()) {
                throw new IllegalArgumentException("Status details page failed validation");
            }
            pending.result().complete(details);
        } catch (Exception e) {
            pending.result().completeExceptionally(e);
        }
    }

    private void updateStatusTimestamp(String instanceId) {
        lastStatusUpdatedAt.compute(instanceId, (ignored, previous) ->
                Math.max(System.currentTimeMillis(), previous == null ? 0 : previous + 1));
    }

    private boolean isValidServerStatus(JsonNode status) {
        if (!status.isObject()
                || !status.path("navigatorStatus").isObject()
                || !status.path("navigatorStatus").path("lastUpdate").isTextual()
                || !status.path("commandDeque").isTextual()
                || !status.path("notifications").isTextual()
                || !status.path("activeThreads").isArray()) {
            return false;
        }
        JsonNode navigatorStatus = status.path("navigatorStatus");
        if (!navigatorStatus.path("fleetCount").canConvertToInt()
                && !navigatorStatus.path("fleetStatus").isTextual()) {
            return false;
        }
        for (JsonNode thread : status.path("activeThreads")) {
            if (!thread.isTextual()) {
                return false;
            }
        }
        return true;
    }

    private void removeExpiredStatusRequests() {
        long expirationTime = System.currentTimeMillis() - STATUS_REQUEST_TIMEOUT_MILLIS;
        pendingStatusRequests.entrySet().removeIf(entry -> entry.getValue().requestedAt() < expirationTime);
    }

    private boolean isConfigResponse(String type) {
        return "response:config:list".equals(type)
                || "response:config:definitions".equals(type)
                || "response:config:set".equals(type)
                || "response:config:delete".equals(type);
    }

    private record PendingStatusRequest(String instanceId, String sessionId, long requestedAt) {
    }

    private record PendingStatusDetailsRequest(String instanceId, String sessionId,
                                               int requestedOffset, int requestedLimit,
                                               CompletableFuture<StatusDetails> result) {
    }
}