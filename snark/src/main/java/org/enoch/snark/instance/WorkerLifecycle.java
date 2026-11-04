package org.enoch.snark.instance;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.enoch.snark.ws.SnarkWebSocketClient;
import org.enoch.snark.instance.si.Core;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class WorkerLifecycle {

    private final ConfigurableApplicationContext applicationContext;
    private final SnarkWebSocketClient webSocketClient;
    private final Core core;
    private final AtomicBoolean shuttingDown = new AtomicBoolean();

    public WorkerLifecycle(ConfigurableApplicationContext applicationContext,
                           SnarkWebSocketClient webSocketClient,
                           Core core) {
        this.applicationContext = applicationContext;
        this.webSocketClient = webSocketClient;
        this.core = core;
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

        closeBrowserSessions();
        exitApplication(1);
    }

    public void stop() {
        if (!shuttingDown.compareAndSet(false, true)) {
            System.out.println("[SHUTDOWN][LIFECYCLE] Stop ignored; shutdown is already in progress");
            return;
        }
        System.out.println("Stopping snark instance on conductor request");
        closeBrowserSessions();
        exitApplication(0);
    }

    private void closeBrowserSessions() {
        long startedAt = System.nanoTime();
        try {
            core.closeBrowserSessions();
        } catch (RuntimeException e) {
            System.err.println("[SHUTDOWN][LIFECYCLE] Failed while closing browser sessions: " + e.getMessage());
            e.printStackTrace(System.err);
        }
        System.out.println("[SHUTDOWN][LIFECYCLE] Browser close phase finished in "
                + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt) + " ms");
    }

    private void exitApplication(int exitCode) {
        long startedAt = System.nanoTime();
        System.out.println("[SHUTDOWN][LIFECYCLE] Closing Spring application context");
        try {
            int applicationExitCode = SpringApplication.exit(applicationContext, () -> exitCode);
            System.out.println("[SHUTDOWN][LIFECYCLE] Spring application context closed in "
                    + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)
                    + " ms; exiting with code " + applicationExitCode);
            System.exit(applicationExitCode);
        } catch (RuntimeException e) {
            System.err.println("Graceful snark shutdown failed: " + e.getMessage());
            e.printStackTrace(System.err);
            Runtime.getRuntime().halt(1);
        }
    }

    public void notifyLoggedOutAndWaiting(String retryAt) {
        webSocketClient.publishLoggedOutState(retryAt);
    }

    public void notifyStarting() {
        webSocketClient.publishStartingState();
    }

    public void notifyRunning() {
        webSocketClient.publishRunningState();
    }

    public String buildRetryMessage(LocalDateTime retryAt) {
        return "Retry at " + retryAt.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
