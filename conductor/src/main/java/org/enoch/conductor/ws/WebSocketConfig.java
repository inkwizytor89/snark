package org.enoch.conductor.ws;

import org.enoch.conductor.api.TerminalWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WorkerSocketHandler handler;
    private final TerminalWebSocketHandler terminalHandler;

    public WebSocketConfig(WorkerSocketHandler handler, TerminalWebSocketHandler terminalHandler) {
        this.handler = handler;
        this.terminalHandler = terminalHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        registry.addHandler(handler, "/ws")
                .setAllowedOrigins("*");
        
        registry.addHandler(terminalHandler, "/ws/terminal")
                .setAllowedOrigins("*");
    }
}
