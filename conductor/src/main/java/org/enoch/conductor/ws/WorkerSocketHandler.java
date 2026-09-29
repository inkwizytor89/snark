package org.enoch.conductor.ws;

import org.enoch.conductor.command.CommandMessage;
import org.enoch.conductor.config.InstanceConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WorkerSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, WebSocketSession> workers = new ConcurrentHashMap<>();
    private final Map<String, String> lastStatusMessages = new ConcurrentHashMap<>();
    private final Map<String, Long> lastStatusUpdatedAt = new ConcurrentHashMap<>();
    
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

        if (type.equals("REGISTER")) {

            String instanceId = json.path("instanceId").asText("");

            workers.put(instanceId, session);

            System.out.println("[WS][WORKER][REGISTERED] instanceId=" + instanceId + " sessionId=" + session.getId());
            return;
        }

        if (isStatusMessage(type)) {
            String instanceId = resolveInstanceId(json);
            String statusMessage = extractStatusMessage(json);

            if (instanceId != null && !statusMessage.isBlank()) {
                lastStatusMessages.put(instanceId, statusMessage);
                lastStatusUpdatedAt.put(instanceId, System.currentTimeMillis());
            }
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

        workers.values().remove(session);
        if (instanceId != null && instanceConfigService != null) {
            lastStatusMessages.remove(instanceId);
            lastStatusUpdatedAt.remove(instanceId);
            instanceConfigService.clearInstanceState(instanceId);
            instanceConfigService.cancelPendingRequests(instanceId);
        }
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

        CommandMessage msg = new CommandMessage();
        msg.setId(UUID.randomUUID().toString());
        msg.setType("request:status");
        msg.setFrom("conductor");
        msg.setTo(workerId);
        msg.setTimestamp(System.currentTimeMillis());
        msg.setStatus(100);

        String json = mapper.writeValueAsString(msg);
        logOutgoing(session, workerId, json);
        session.sendMessage(new TextMessage(json));
        return true;
    }

    public String getLastStatusMessage(String workerId) {

        return lastStatusMessages.get(workerId);
    }

    public long getLastStatusUpdatedAt(String workerId) {

        return lastStatusUpdatedAt.getOrDefault(workerId, 0L);
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
        return "STATUS".equalsIgnoreCase(type)
                || "response:status".equalsIgnoreCase(type)
                || type.endsWith(":status");
    }

    private boolean isConfigResponse(String type) {
        return "response:config:list".equalsIgnoreCase(type)
                || "response:config:definitions".equalsIgnoreCase(type)
                || "response:config:set".equalsIgnoreCase(type)
                || "response:config:delete".equalsIgnoreCase(type);
    }

    private String resolveInstanceId(JsonNode json) {
        String instanceId = json.path("instanceId").asText("");

        if (!instanceId.isBlank()) {
            return instanceId;
        }

        instanceId = json.path("from").asText("");
        if (!instanceId.isBlank()) {
            return instanceId;
        }

        instanceId = json.path("to").asText("");
        return instanceId.isBlank() ? null : instanceId;
    }

    private String extractStatusMessage(JsonNode json) {
        JsonNode payload = json.path("payload");

        if (payload.isMissingNode() || payload.isNull()) {
            String message = json.path("message").asText("");
            if (!message.isBlank()) {
                return message;
            }

            String statusMessage = json.path("statusMessage").asText("");
            if (!statusMessage.isBlank()) {
                return statusMessage;
            }

            return "";
        }

        if (payload.isTextual() || payload.isValueNode()) {
            return payload.asText("");
        }

        if (payload.isObject()) {
            for (String field : new String[] { "message", "statusMessage", "text", "status", "state" }) {
                JsonNode candidate = payload.path(field);
                if (!candidate.isMissingNode() && !candidate.isNull()) {
                    String text = candidate.asText("");
                    if (!text.isBlank()) {
                        return text;
                    }
                }
            }
        }

        return payload.toString();
    }
}