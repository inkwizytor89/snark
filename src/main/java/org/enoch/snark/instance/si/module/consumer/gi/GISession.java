package org.enoch.snark.instance.si.module.consumer.gi;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.instance.si.module.consumer.Credentials;

public class GISession {

    public GI gi;
    private SessionGIR gir;

    private boolean isRunning;

    public GISession(GI gi) {
        this.gi = gi;
        gir = new SessionGIR(gi);
    }

//    GISession powinien zrezygnowac z Credentials i korzystac z mapy i chyba polaczone powinnoi zostac z gir
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
                gi.reopenWebDriver();
                gir.manageDriver();
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
        return !isRunning || gir.isCurrentUrlEmpty() || gir.isCurrentUrlBackToLobby();
    }

    public String reopenServerIfSessionIsOver(Credentials credentials) {
        if (!isRunning) {
            return start(credentials);
        }

        if (gir.isCurrentUrlBackToLobby()) {
            isRunning = false;
            System.err.println("before restart sleep " + 300);
            SleepUtil.secondsToSleep(300L);
            return start(credentials);
        }

        return credentials.token();
    }
}
