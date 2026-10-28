package org.enoch.snark.instance;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.enoch.snark.ws.SnarkWebSocketClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class WorkerLifecycle {

    private final ConfigurableApplicationContext applicationContext;
    private final SnarkWebSocketClient webSocketClient;
    private final AtomicBoolean shuttingDown = new AtomicBoolean();

    public WorkerLifecycle(ConfigurableApplicationContext applicationContext,
                           SnarkWebSocketClient webSocketClient) {
        this.applicationContext = applicationContext;
        this.webSocketClient = webSocketClient;
    }

    public void terminate(String reason) {
        if (!shuttingDown.compareAndSet(false, true)) {
            return;
        }
        System.err.println("Terminating snark instance: " + reason);
        webSocketClient.publishRestartRequired(reason);
        Thread forcedExit = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            Runtime.getRuntime().halt(1);
        }, "snark-forced-exit");
        forcedExit.setDaemon(true);
        forcedExit.start();

        try {
            int exitCode = SpringApplication.exit(applicationContext, () -> 1);
            System.exit(exitCode);
        } catch (RuntimeException e) {
            System.err.println("Graceful snark shutdown failed: " + e.getMessage());
            e.printStackTrace(System.err);
            Runtime.getRuntime().halt(1);
        }
    }
}
