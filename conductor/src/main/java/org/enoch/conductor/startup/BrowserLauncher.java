package org.enoch.conductor.startup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class BrowserLauncher {

    private final int serverPort;

    public BrowserLauncher(@Value("${server.port:8080}") int serverPort) {
        this.serverPort = serverPort;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openDefaultBrowser() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            return;
        }

        try {
            desktop.browse(new URI("http://localhost:" + serverPort + "/"));
        } catch (URISyntaxException | java.io.IOException e) {
            throw new IllegalStateException("Failed to open default browser", e);
        }
    }
}
