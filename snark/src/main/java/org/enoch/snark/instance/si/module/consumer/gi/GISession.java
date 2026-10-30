package org.enoch.snark.instance.si.module.consumer.gi;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.instance.si.module.consumer.Credentials;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.enoch.snark.instance.si.module.consumer.gi.SessionGIR.LOBBY_URL;

public class GISession {

    @Getter
    public Wd wd;
    private final org.enoch.snark.instance.WorkerLifecycle workerLifecycle;
    private SessionGIR gir;

    private boolean isRunning;
    private String serverWindowHandle;

    public GISession(String pathToDriver, org.enoch.snark.instance.WorkerLifecycle workerLifecycle) {
        this.workerLifecycle = workerLifecycle;
        this.wd = new Wd(pathToDriver);
        gir = new SessionGIR(wd);
    }

    private String start(Credentials credentials) {
        isRunning = false;
        long nextDelaySeconds = 30L;
        while(!isRunning) {
            wd.requireDriverAlive();
            try {
                String lobbyToken = credentials.token();
                gir.wd.reopenWebDriver(LOBBY_URL);
                gir.applyCookies(lobbyToken);
                if (!gir.isCurrentUrlLobbyAccount()) {
                    lobbyToken = gir.signInWithRetry(credentials);
                    if (StringUtils.EMPTY.equals(lobbyToken)) {
                        waitBeforeRetry(nextDelaySeconds);
                        nextDelaySeconds = Math.min(nextDelaySeconds * 2, 300L);
                        continue;
                    }
                }
                if (!gir.openServer()) {
                    wd.requireDriverAlive();
                    waitBeforeRetry(nextDelaySeconds);
                    nextDelaySeconds = Math.min(nextDelaySeconds * 2, 300L);
                    continue;
                }
                serverWindowHandle = wd.getWebDriver().getWindowHandle();
                isRunning = true;
                return lobbyToken;
            } catch (Throwable e) {
                if (e instanceof BrowserWindowUnavailableException browserWindowUnavailable) {
                    throw browserWindowUnavailable;
                }
                wd.requireDriverAlive();
                e.printStackTrace();
                waitBeforeRetry(nextDelaySeconds);
                nextDelaySeconds = Math.min(nextDelaySeconds * 2, 300L);
            }
        }
        throw new RuntimeException("Could not start session");
    }

    public boolean isNeededToRestart() {
        wd.requireDriverAlive();
        if (isRunning) {
            wd.requireWindow(serverWindowHandle);
        }
        boolean emptyUrl = isRunning && gir.isCurrentUrlEmpty();
        boolean backToLobby = isRunning && !emptyUrl && gir.isCurrentUrlBackToLobby();
        boolean toCheck = !isRunning || emptyUrl || backToLobby;
        if(toCheck) System.err.println("!isRunning "+!isRunning +" gir.isCurrentUrlEmpty() "+ emptyUrl +" gir.isCurrentUrlBackToLobby() "+ backToLobby);
        return toCheck;
    }

    public boolean isActiveServerWindowAvailable() {
        return !isRunning || wd.hasWindow(serverWindowHandle);
    }

    public boolean isCurrentUrlBackToLobby() {
        return gir.isCurrentUrlBackToLobby();
    }

    public String openNewServerSession(Credentials credentials) {
        wd.requireDriverAlive();
        if (isRunning) {
            wd.requireWindow(serverWindowHandle);
        }
        if (!isRunning) {
            System.err.println("!isRunning");
            return start(credentials);
        }

        if (gir.isCurrentUrlBackToLobby()) {
            System.err.println("Server session returned to lobby; waiting before reconnecting");
            isRunning = false;
            long retrySeconds = credentials.restartDuration().getSeconds();
            LocalDateTime retryAt = LocalDateTime.now().plusSeconds(retrySeconds);
            String retryText = workerLifecycle.buildRetryMessage(retryAt);
            workerLifecycle.notifyLoggedOutAndWaiting(retryText);
            waitBeforeRetry(retrySeconds);
            workerLifecycle.notifyStarting();
            return start(credentials);
        }

        if (gir.isCurrentUrlEmpty()) {
            isRunning = false;
            return start(credentials);
        }

        return credentials.token();
    }

    private void waitBeforeRetry(long seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting to restart the game session", e);
        }
    }
}
