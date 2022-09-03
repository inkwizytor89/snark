package org.enoch.snark.gi;

import org.enoch.snark.exception.GIException;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.Utils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;

import static org.enoch.snark.gi.text.HtmlElements.*;

public class GISession {

    private final WebDriver webDriver;
    private final Instance instance;
    public GI gi;

    private boolean isLoggedIn = false;

    public GISession(Instance instance) {
        this.instance = instance;
        gi = instance.gi;
        webDriver = gi.webDriver;

        webDriver.manage().window().maximize();

        while(!isLoggedIn) {
            try {
                open();
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        switchToServerTab();
    }

    private void switchToServerTab() {
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
            instance.daoFactory.errorDAO.save(e);
        }
    }

    public void close() {
        logOut();
    }

    private void logIn() {

        //insertLoginData
//        gi.findText("Login").click();
//        gi.findElement(TAG_INPUT, ATTRIBUTE_NAME, Marker.LOGIN_MAIL_NAME).sendKeys(instance.universeEntity.login);
//        gi.findElement(TAG_INPUT, ATTRIBUTE_NAME, Marker.LOGIN_PASSWORD_NAME).sendKeys(instance.universeEntity.pass);
//        gi.findElement(TAG_BUTTON, ATTRIBUTE_TYPE, VALUE_SUBMIT).click();

//        gi.findText("Login").click();
        gi.findElement(TAG_INPUT, "id", "QA_SignIn_Email_Input").sendKeys(instance.universe.login);
        gi.findElement(TAG_BUTTON, "id", "QA_SignIn_Next_Button").click();
        Utils.secondsToSleep(2L);


        gi.findElement(TAG_INPUT, "id", "QA_SignIn_Password_Input").sendKeys(instance.universe.pass);
        gi.findElement(TAG_BUTTON, "id", "QA_SignIn_SignIn_Button").click();
        Utils.secondsToSleep(2L);

        //chooseServer
        webDriver.get("https://lobby.ogame.gameforge.com/pl_PL/accounts"); //gi.clickText(Test.PLAY_TEXT);
        gi.doubleClickText(instance.universe.name);

        isLoggedIn = true;
    }

    private void logOut() {
        new GIUrlBuilder(instance).openOverview();
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
