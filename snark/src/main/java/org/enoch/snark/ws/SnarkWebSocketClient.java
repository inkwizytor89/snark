package org.enoch.snark.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.enoch.common.command.CommandMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class SnarkWebSocketClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final String managerUrl;
    private final String instanceId;

    private volatile WebSocket webSocket;

    public SnarkWebSocketClient(@Value("${managerUrl:}") String managerUrl,
                                @Value("${instanceId:${spring.application.name:snark}}") String instanceId) {
        this.managerUrl = managerUrl == null ? "" : managerUrl.trim();
        this.instanceId = instanceId == null || instanceId.isBlank() ? "snark" : instanceId.trim();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void connect() {
        if (managerUrl.isBlank()) {
            System.out.println("[WS][SKIP] managerUrl is empty, WebSocket client not started");
            return;
        }

        Request request = new Request.Builder()
                .url(managerUrl)
                .build();

        System.out.println("[WS][CONNECTING] managerUrl=" + managerUrl + " instanceId=" + instanceId);
        webSocket = okHttpClient.newWebSocket(request, new WorkerListener());
    }

    @PreDestroy
    public void shutdown() {
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
            System.out.println("[WS][OUT] " + json);
            socket.send(json);
        } catch (Exception e) {
            System.err.println("[WS][ERROR] Failed to send message: " + e.getMessage());
            e.printStackTrace(System.err);
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
            System.out.println("[WS][OPEN] managerUrl=" + managerUrl + " instanceId=" + instanceId);
            sendAndLog(webSocket, buildRegistrationMessage());
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            System.out.println("[WS][IN] " + text);
            try {
                CommandMessage incoming = objectMapper.readValue(text, CommandMessage.class);
                sendAndLog(webSocket, buildEchoResponse(incoming));
            } catch (Exception e) {
                System.err.println("[WS][ERROR] Failed to parse incoming message: " + e.getMessage());
                e.printStackTrace(System.err);
                sendAndLog(webSocket, buildParseError(text, e));
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            System.out.println("[WS][CLOSING] code=" + code + " reason=" + reason);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            System.out.println("[WS][CLOSED] code=" + code + " reason=" + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            System.err.println("[WS][FAILURE] managerUrl=" + managerUrl + " instanceId=" + instanceId
                    + " message=" + t.getMessage());
            if (response != null) {
                System.err.println("[WS][FAILURE] httpStatus=" + response.code());
            }
            t.printStackTrace(System.err);
        }
    }
}
