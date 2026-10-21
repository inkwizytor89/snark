package org.enoch.snark.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PreDestroy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.enoch.common.command.CommandMessage;
import org.enoch.snark.instance.si.Core;
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

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final String managerUrl;
    private final String instanceId;
    private final Core core;

    private volatile WebSocket webSocket;
    private volatile String registrationRequestId;
    private volatile boolean registered;

    public SnarkWebSocketClient(@Value("${managerUrl:}") String managerUrl,
                                @Value("${instanceId:${spring.application.name:snark}}") String instanceId,
                                Core core) {
        this.managerUrl = managerUrl == null ? "" : managerUrl.trim();
        this.instanceId = instanceId == null || instanceId.isBlank() ? "snark" : instanceId.trim();
        this.core = core;
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
                "serverStatus", core.status(),
                "hostname", resolveHostname(),
                "version", resolveVersion()
        ));
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
            if (!socket.send(json)) {
                System.err.println("[WS][ERROR] WebSocket rejected outgoing message type=" + message.getType()
                        + " id=" + message.getId());
            }
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
            System.out.println("[WS][CLOSED] code=" + code + " reason=" + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            registered = false;
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
}
