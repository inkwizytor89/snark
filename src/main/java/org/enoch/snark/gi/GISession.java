package org.enoch.snark.gi;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.instance.Commander;
import org.openqa.selenium.*;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.enoch.snark.gi.SessionGIR.LOBBY_URL;

public class GISession {

    private static GISession INSTANCE;

    private WebDriver webDriver;
    public GI gi;
    private SessionGIR gir;

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
        startBrowserWindow();
        gir = new SessionGIR();
        gir.signIn();
        gir.openServer();
    }

    private void startBrowserWindow() {
        webDriver = GI.reopenWebDriver();
        webDriver.manage().window().maximize();
        webDriver.manage().timeouts().pageLoadTimeout(4, TimeUnit.SECONDS);
    }

    public void reopenServerIfSessionIsOver() {
        Commander commander = Commander.getInstance();
        try {
            if (isCurrentUrlBackToLobby()) {
                commander.stopCommander();
                System.err.println("sleep 300 before restart");
                SleepUtil.secondsToSleep(300L);
                gir.openServer();
                restart();
                commander.startCommander();
            }
        } catch (WebDriverException e) {
            e.printStackTrace();
            System.err.println("URL error: "+e.getMessage());
            gir.openServer();
//            stopCommander();
//            System.err.println("sleep 500 before restart");
//            SleepUtil.secondsToSleep(500);
//            instance.browserReset();
//            startCommander();
        }
    }

    private void restart() {
        Set<Cookie> cookies = webDriver.manage().getCookies();
        close();
        startBrowserWindow();
        webDriver.get("https://gameforge.com/pl-PL/sign-in");
        cookies.forEach(cookie -> webDriver.manage().addCookie(cookie));
        new GIUrlBuilder().openComponent(GIUrlBuilder.PAGE_OVERVIEW, null);
    }

    private void close() {
        boolean isException;
        do {
            try {
                GI.closeWebDriver();
                isException = false;
            } catch (Throwable e) {
                e.printStackTrace();
                isException = true;
                SleepUtil.sleep(TimeUnit.MINUTES, 1);
            }
        } while(isException);
    }

    public boolean isCurrentUrlBackToLobby() {
        return GI.webDriver.getCurrentUrl().contains(LOBBY_URL);
    }

    public WebDriver getWebDriver() {
        return webDriver;
    }

}
