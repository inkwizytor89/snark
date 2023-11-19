package org.enoch.snark.gi;

import org.enoch.snark.db.dao.ErrorDAO;
import org.enoch.snark.exception.GIException;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.common.SleepUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.enoch.snark.gi.text.HtmlElements.*;
import static org.enoch.snark.instance.config.Config.*;

public class GISession {

    private final WebDriver webDriver;
    public GI gi;

    private boolean isLoggedIn = false;

    public GISession(Instance instance) {
        gi = Instance.gi;
        webDriver = gi.webDriver;

        webDriver.manage().window().maximize();

        while(!isLoggedIn) {
            try {
                open();
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        addNewTabForServer();
    }

    public void addNewTabForServer() {
        ArrayList<String> tabs = new ArrayList<>(webDriver.getWindowHandles());
        for (int i = 1; i<tabs.size(); i++) {
            webDriver.switchTo().window(tabs.get(i));
            SleepUtil.pause();
            webDriver.close();
            SleepUtil.pause();
        }
        webDriver.switchTo().window(tabs.get(0));
        webDriver.get("https://lobby.ogame.gameforge.com/pl_PL/accounts"); //gi.clickText(Test.PLAY_TEXT);
        SleepUtil.sleep();
        gi.doubleClickText(Instance.config.getConfig(SERVER));
        SleepUtil.sleep(TimeUnit.SECONDS,4);

        String server = new ArrayList<>(webDriver.getWindowHandles()).get(1);
        webDriver.switchTo().window(server);
    }

    public void open() {
//        webDriver.get("https://lobby.ogame.gameforge.com/pl_PL/");//instance.universeEntity.url);
        webDriver.get("https://gameforge.com/pl-PL/sign-in");//instance.universeEntity.url);
        try {
            Cookie cookie = new Cookie("gf-cookie-consent-4449562312", "|7|1",".gameforge.com",
                    "/", null);
            webDriver.manage().addCookie(cookie);

            logIn();
        } catch (GIException e) {
            System.err.println(e.getMessage());
            ErrorDAO.getInstance().save(e);
        }
    }

    public void close() {
        logOut();
    }

    private void logIn() {
    webDriver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
//    webDriver.manage().timeouts().setScriptTimeout(40, TimeUnit.SECONDS);
        //insertLoginData
//        gi.findText("Login").click();
//        gi.findElement(TAG_INPUT, ATTRIBUTE_NAME, Marker.LOGIN_MAIL_NAME).sendKeys(instance.universeEntity.login);
//        gi.findElement(TAG_INPUT, ATTRIBUTE_NAME, Marker.LOGIN_PASSWORD_NAME).sendKeys(instance.universeEntity.pass);
//        gi.findElement(TAG_BUTTON, ATTRIBUTE_TYPE, VALUE_SUBMIT).click();

//        gi.findText("Login").click();
        gi.findElement(TAG_INPUT, "id", "QA_SignIn_Email_Input").sendKeys(Instance.config.getConfig(LOGIN));
        gi.findElement(TAG_BUTTON, "id", "QA_SignIn_Next_Button").click();
        SleepUtil.secondsToSleep(2);

//        try {
            gi.findElement(TAG_INPUT, "id", "QA_SignIn_Password_Input").sendKeys(Instance.config.getConfig(PASSWORD));
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
        isLoggedIn = true;


    }

    private void logOut() {
        new GIUrlBuilder().openComponent(GIUrlBuilder.PAGE_OVERVIEW, null);
        webDriver.findElement(By.linkText("Wyloguj")).click();
        isLoggedIn = false;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public WebDriver getWebDriver() {
        return webDriver;
    }

}
