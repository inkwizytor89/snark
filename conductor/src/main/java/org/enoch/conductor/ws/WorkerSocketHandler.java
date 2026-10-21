package org.enoch.conductor.ws;

import org.enoch.common.command.CommandMessage;
import org.enoch.common.status.NavigatorStatus;
import org.enoch.common.status.ServerStatus;
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
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WorkerSocketHandler extends TextWebSocketHandler {

    private static final long STATUS_REQUEST_TIMEOUT_MILLIS = 60_000L;

    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final Map<String, WebSocketSession> workers = new ConcurrentHashMap<>();
    private final Map<String, ServerStatus> lastStatuses = new ConcurrentHashMap<>();
    private final Map<String, Long> lastStatusUpdatedAt = new ConcurrentHashMap<>();
    private final Map<String, PendingStatusRequest> pendingStatusRequests = new ConcurrentHashMap<>();
    
    private InstanceConfigService instanceConfigService;

    public void setInstanceConfigService(InstanceConfigService service) {
        this.instanceConfigService = service;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("[WS][WORKER][CONNECTED] sessionId=" + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
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

        if (isStatusMessage(type)) {
            storeStatusResponse(registeredInstanceId, json);
        }

        if (isConfigResponse(type) && instanceConfigService != null) {
            CommandMessage response = mapper.treeToValue(json, CommandMessage.class);
            instanceConfigService.handleConfigResponse(response);
        }

        if (type.equals("LOG")) {
            System.out.println("[WORKER] " + json.path("message").asText(""));
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
            pendingStatusRequests.entrySet().removeIf(entry -> instanceId.equals(entry.getValue().instanceId()));
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
        lastStatuses.remove(instanceId);
        lastStatusUpdatedAt.remove(instanceId);
        pendingStatusRequests.entrySet().removeIf(entry -> instanceId.equals(entry.getValue().instanceId()));
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
        pendingStatusRequests.put(msg.getId(), new PendingStatusRequest(workerId, System.currentTimeMillis()));
        logOutgoing(session, workerId, json);
        try {
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            pendingStatusRequests.remove(msg.getId());
            throw e;
        }
        return true;
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

    private void storeStatusResponse(String instanceId, JsonNode json) {
        String requestId = json.path("id").asText("");
        PendingStatusRequest pending = pendingStatusRequests.remove(requestId);
        if (pending == null
                || !instanceId.equals(pending.instanceId())
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
                    ""
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
            lastStatuses.put(instanceId, mapper.treeToValue(serverStatusNode, ServerStatus.class));
        } catch (Exception e) {
            System.err.println("[WS][WORKER][REJECTED] Invalid server status instanceId=" + instanceId
                    + " requestId=" + requestId + ": " + e.getMessage());
            return;
        }
        updateStatusTimestamp(instanceId);
    }

    private void updateStatusTimestamp(String instanceId) {
        lastStatusUpdatedAt.compute(instanceId, (ignored, previous) ->
                Math.max(System.currentTimeMillis(), previous == null ? 0 : previous + 1));
    }

    private boolean isValidServerStatus(JsonNode status) {
        if (!status.isObject()
                || !status.path("navigatorStatus").isObject()
                || status.path("navigatorStatus").path("fleetStatus").asText("").isBlank()
                || !status.path("navigatorStatus").path("lastUpdate").isTextual()
                || !status.path("commandDeque").isTextual()
                || !status.path("notifications").isTextual()
                || !status.path("activeThreads").isArray()) {
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

    private record PendingStatusRequest(String instanceId, long requestedAt) {
    }
}