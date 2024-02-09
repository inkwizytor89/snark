package org.enoch.snark.gi;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.ErrorDAO;
import org.enoch.snark.exception.GIException;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.instance.Commander;
import org.enoch.snark.instance.Instance;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.enoch.snark.gi.text.HtmlElements.TAG_BUTTON;
import static org.enoch.snark.gi.text.HtmlElements.TAG_INPUT;
import static org.enoch.snark.instance.config.Config.*;

public class GISession {
    public static final String LOBBY_URL = "https://lobby.ogame.gameforge.com/";

    private static GISession INSTANCE;

    private WebDriver webDriver;
    public GI gi;

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

    public void start() {
        startBrowserWindow();
        loginToServer();
    }

    public void startBrowserWindow() {
        webDriver = gi.reopenWebDriver();
        webDriver.manage().window().maximize();
    }

    public void loginToServer() {
        boolean isLoggedIn = false;
        while(!isLoggedIn) {
            try {
                webDriver.get("https://gameforge.com/pl-PL/sign-in");//instance.universeEntity.url);
                try {
                    Cookie cookie = new Cookie("gf-cookie-consent-4449562312", "|7|1",".gameforge.com",
                            "/", null);
                    webDriver.manage().addCookie(cookie);

                    typeCredentials();
                    isLoggedIn = true;
                } catch (GIException e) {
                    System.err.println(e.getMessage());
                    ErrorDAO.getInstance().save(e);
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        selectServer();
    }

    public void close() {
        boolean isException;
        do {
            try {
                GI.closeWebDriver();
//                gi = GI.getInstance();
                isException = false;
            } catch (Throwable e) {
                e.printStackTrace();
                isException = true;
                SleepUtil.sleep(TimeUnit.MINUTES, 1);
            }
        } while(isException);
    }

    public void restart() {
        Set<Cookie> cookies = webDriver.manage().getCookies();
        close();
        startBrowserWindow();
        webDriver.get("https://gameforge.com/pl-PL/sign-in");
        cookies.forEach(cookie -> webDriver.manage().addCookie(cookie));
//        SleepUtil.secondsToSleep(4L);
        new GIUrlBuilder().openComponent(GIUrlBuilder.PAGE_OVERVIEW, null);
//        loginToServer();
    }

    public void selectServer() {
        ArrayList<String> tabs = new ArrayList<>(webDriver.getWindowHandles());
        closeOtherTabs(tabs);
        webDriver.switchTo().window(tabs.get(0));

        webDriver.get("https://lobby.ogame.gameforge.com/pl_PL/accounts"); //gi.clickText(Test.PLAY_TEXT);
        SleepUtil.sleep();
        gi.doubleClickText(Instance.getMainConfigMap().getConfig(SERVER));
        SleepUtil.sleep(TimeUnit.SECONDS,4);

        String server = new ArrayList<>(webDriver.getWindowHandles()).get(1);
        webDriver.switchTo().window(server);
    }

    public void closeOtherTabs(ArrayList<String> tabs) {
        for (int i = 1; i<tabs.size(); i++) {
            webDriver.switchTo().window(tabs.get(i));
            SleepUtil.pause();
            webDriver.close();
            SleepUtil.pause();
        }
    }

    private void typeCredentials() {
    webDriver.manage().timeouts().pageLoadTimeout(4, TimeUnit.SECONDS);
//    webDriver.manage().timeouts().setScriptTimeout(40, TimeUnit.SECONDS);
        //insertLoginData
//        gi.findText("Login").click();
//        gi.findElement(TAG_INPUT, ATTRIBUTE_NAME, Marker.LOGIN_MAIL_NAME).sendKeys(instance.universeEntity.login);
//        gi.findElement(TAG_INPUT, ATTRIBUTE_NAME, Marker.LOGIN_PASSWORD_NAME).sendKeys(instance.universeEntity.pass);
//        gi.findElement(TAG_BUTTON, ATTRIBUTE_TYPE, VALUE_SUBMIT).click();

//        gi.findText("Login").click();
        gi.findElement(TAG_INPUT, "id", "QA_SignIn_Email_Input").sendKeys(Instance.getMainConfigMap().getConfig(LOGIN));
        gi.findElement(TAG_BUTTON, "id", "QA_SignIn_Next_Button").click();
        SleepUtil.secondsToSleep(2);

//        try {
            gi.findElement(TAG_INPUT, "id", "QA_SignIn_Password_Input").sendKeys(Instance.getMainConfigMap().getConfig(PASSWORD));
            gi.findElement(TAG_BUTTON, "id", "QA_SignIn_SignIn_Button").click();//        webDriver.navigate().refresh();
            SleepUtil.secondsToSleep(10);

//        String expectedURL="gameforge.com/pl-PL";
//        for(int i =0; i<20;i++) {
//            SleepUtil.secondsToSleep(1);
//            if(expectedURL.contains(webDriver.getCurrentUrl())) {
//                System.err.println("correct page");
//                break;
//            }
//            System.err.println(webDriver.getCurrentUrl() + " is not contains " + expectedURL);
//        }

//            webDriver.manage().addCookie(new Cookie("OACCAP", "6160.1", "ads-delivery.gameforge.com", "/", null));
//            webDriver.manage().addCookie(new Cookie("OACBLOCK", "6160.1698704752", "ads-delivery.gameforge.com", "/", null));
//            webDriver.manage().addCookie(new Cookie("OAID", "0abaf6a728a0c583382ef25c4dd72e93", "ads-delivery.gameforge.com", "/", null));


    }

    public void reopenServerIfSessionIsOver() {
        Commander commander = Commander.getInstance();
        try {
            if (isCurrentUrlBackToLobby()) {
                commander.stopCommander();
                System.err.println("sleep 300 before restart");
                SleepUtil.secondsToSleep(300L);
                selectServer();
                restart();
                commander.startCommander();
            }
        } catch (WebDriverException e) {
            e.printStackTrace();
            System.err.println("URL error: "+e.getMessage());
            selectServer();
//            stopCommander();
//            System.err.println("sleep 500 before restart");
//            SleepUtil.secondsToSleep(500);
//            instance.browserReset();
//            startCommander();
        }
    }

    public boolean isCurrentUrlBackToLobby() {
        return gi.webDriver.getCurrentUrl().contains(LOBBY_URL);
    }

    public WebDriver getWebDriver() {
        return webDriver;
    }

}
