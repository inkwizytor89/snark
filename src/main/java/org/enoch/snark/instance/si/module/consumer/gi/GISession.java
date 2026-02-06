package org.enoch.snark.instance.si.module.consumer.gi;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.instance.si.module.consumer.Credentials;

import java.time.LocalDateTime;

import static org.enoch.snark.instance.si.module.consumer.gi.SessionGIR.LOBBY_URL;

public class GISession {

    @Getter
    public Wd wd;
    private SessionGIR gir;

    private boolean isRunning;

    public GISession(String pathToDriver) {
        this.wd = new Wd(pathToDriver);
        gir = new SessionGIR(wd);
    }

    private String start(Credentials credentials) {
        isRunning = false;
        long waitingSecounds = 30L;
        long nexRound = 50L;
        while(!isRunning) {
            try {
                if (waitingSecounds > 30L) {
                    System.err.println("Next attempt in "+nexRound);
                    SleepUtil.secondsToSleep(nexRound);
                }
                long tmp = nexRound;
                nexRound = nexRound + waitingSecounds;
                waitingSecounds = tmp;

                String lobbyToken = credentials.token();
                gir.wd.reopenWebDriver(LOBBY_URL);
                gir.applyCookies(lobbyToken);
                if (!gir.isCurrentUrlLobbyAccount()) {
                    lobbyToken = gir.signInWithRetry(credentials);
                    if (StringUtils.EMPTY.equals(lobbyToken)) continue;
                }
                if (!gir.openServer()) continue;
                isRunning = true;
                return lobbyToken;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("Could not start session");
    }

    public boolean isNeededToRestart() {
        return !isRunning|| !gir.wd.isDriverAlive() || gir.isCurrentUrlEmpty() || gir.isCurrentUrlBackToLobby();
    }

    public String openNewServerSession(Credentials credentials) {
        if (!isRunning) {
            return start(credentials);
        }

        if (!gir.wd.isDriverAlive() || gir.isCurrentUrlBackToLobby()) {
            isRunning = false;
            System.err.println("before restart sleep " + credentials.restartDuration().getSeconds()+" "+ LocalDateTime.now());
            SleepUtil.sleep(credentials.restartDuration());
            System.err.println("after restart sleep " + credentials.restartDuration().getSeconds()+" "+ LocalDateTime.now());
            return start(credentials);
        }

        return credentials.token();
    }
}
