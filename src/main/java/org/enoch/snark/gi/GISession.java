package org.enoch.snark.gi;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.instance.commander.Commander;

public class GISession {

    private static GISession INSTANCE;

    public GI gi;
    private SessionGIR gir;

    private boolean isRunning;

    public static GISession getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new GISession();
        }
        return INSTANCE;
    }

    private GISession() {
        gi = GI.getInstance();
        start();
    }

    private void start() {
        isRunning = false;
        boolean failAction = false;
        while(!isRunning) {
            if(failAction) SleepUtil.secondsToSleep(600L);
            failAction = true;

            GI.reopenWebDriver();
            gir = new SessionGIR();
            gir.manageDriver();
            if(!gir.applyCookies()) continue;
            if(!gir.isCurrentUrlLobbyAccount()) {
                if(!gir.signInWithRetry()) continue;
            }
            if(!gir.openServer()) continue;
            isRunning = true;
        }
    }

    public void reopenServerIfSessionIsOver() {
        if (gir.isCurrentUrlBackToLobby()) {
            makeRestart(300);
        }
    }

    private void makeRestart(long secondsToSleep) {
        Commander commander = Commander.getInstance();
        isRunning = false;
        commander.stopCommander();
        System.err.println("before restart sleep " + secondsToSleep);
        SleepUtil.secondsToSleep(secondsToSleep);
        start();
        commander.startCommander();
    }

    public boolean isRunning() {
        return isRunning;
    }
}
