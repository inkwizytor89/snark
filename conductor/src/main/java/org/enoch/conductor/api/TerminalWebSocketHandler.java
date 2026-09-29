package org.enoch.conductor.api;

import org.enoch.conductor.cli.TerminalCli;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class TerminalWebSocketHandler extends TextWebSocketHandler {

    private final TerminalCli terminalCli;

    private static class TerminalSession {
        WebSocketSession wsSession;
        boolean running;
    }

    private final ConcurrentHashMap<String, TerminalSession> sessions = new ConcurrentHashMap<>();

    public TerminalWebSocketHandler(TerminalCli terminalCli) {
        this.terminalCli = terminalCli;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        TerminalSession ts = new TerminalSession();
        ts.wsSession = session;
        ts.running = false;
        sessions.put(session.getId(), ts);

        System.out.println("[WS][TERMINAL][CONNECTED] sessionId=" + session.getId());
        sendToTerminal(session, "Terminal connected");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        TerminalSession ts = sessions.get(session.getId());
        if (ts == null) {
            return;
        }

        String payload = message.getPayload();
        logIncoming(session, payload);

        try {
            if (payload.startsWith("START:")) {
                String command = payload.substring(6);
                String output = terminalCli.executeCommand(command);
                if (output != null && !output.isBlank()) {
                    for (String line : output.split("\\R")) {
                        sendToTerminal(session, "OUTPUT: " + line);
                    }
                }
                sendToTerminal(session, "EXIT: 0");
            } else if (payload.equals("KILL")) {
                sendToTerminal(session, "OUTPUT: Terminal commands are handled by TerminalCli");
                sendToTerminal(session, "EXIT: 0");
            } else if (payload.startsWith("INPUT:")) {
                sendToTerminal(session, "OUTPUT: INPUT is handled by TerminalCli commands");
                sendToTerminal(session, "EXIT: 0");
            }
        } catch (Exception e) {
            sendToTerminal(session, "ERROR: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        System.out.println("[WS][TERMINAL][DISCONNECTED] sessionId=" + session.getId() + " status=" + status);
    }

    private void sendToTerminal(WebSocketSession session, String payload) throws Exception {
        logOutgoing(session, payload);
        session.sendMessage(new TextMessage(payload));
    }

    private void logIncoming(WebSocketSession session, String payload) {
        System.out.println("[WS][TERMINAL][IN] sessionId=" + session.getId() + " payload=" + payload);
    }

    private void logOutgoing(WebSocketSession session, String payload) {
        System.out.println("[WS][TERMINAL][OUT] sessionId=" + session.getId() + " payload=" + payload);
    }
}

