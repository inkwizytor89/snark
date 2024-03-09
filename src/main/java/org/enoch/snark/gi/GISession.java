package org.enoch.snark.gi;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.instance.commander.Commander;
import org.openqa.selenium.*;

import java.util.HashSet;
import java.util.Set;

public class GISession {

    private static GISession INSTANCE;

    public GI gi;
    private SessionGIR gir;

    private boolean isRunning;
    private Set<Cookie> cookies = new HashSet<>();

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
        GI.reopenWebDriver();
        gir = new SessionGIR();
        gir.manageDriver();
        if(canUseCookies()) {
            gir.applyCookies(cookies);
        } else {
            gir.signIn();
            gir.openServer();
        }
        isRunning = true;
    }

    private boolean canUseCookies() {
        return !cookies.isEmpty() && cookies.stream().anyMatch(cookie -> cookie.getName().contains("PHPSESSID"));
    }

    public void reopenServerIfSessionIsOver() {
        if (gir.isCurrentUrlBackToLobby()) {
            makeRestart(300);
        }
    }

    public void makeRestart(long secondsToSleep) {
        Commander commander = Commander.getInstance();
        isRunning = false;
        commander.stopCommander();
        System.err.println("before restart sleep " + secondsToSleep);
        SleepUtil.secondsToSleep(secondsToSleep);
        cookies = gir.loadCookies();
        start();
        commander.startCommander();
    }

    public boolean isRunning() {
        return isRunning;
    }
}
