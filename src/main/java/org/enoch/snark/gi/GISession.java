package org.enoch.snark.gi;

import org.enoch.snark.Test;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.gi.text.Marker;
import org.enoch.snark.gi.text.Text;
import org.enoch.snark.instance.Instance;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.enoch.snark.instance.PropertyNames.WEBDRIVER_CHROME_DRIVER;

public class GISession {

    private final WebDriver webDriver;
    private final Instance instance;
    public GI gi;

    private boolean isLoggedIn = false;


    public GISession(Instance instance) {
        this.instance = instance;
        gi = instance.gi;
        webDriver = gi.webDriver;

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
        webDriver.get(instance.universeEntity.url);
        logIn();
    }

    public void close() {
        logOut();
    }

    private void logIn() {
        //skipBannersIfExists
        instance.gi.clickTextIfExists(Text.X);
        instance.gi.clickTextIfExists(Text.AGREE);

        //insertLoginData
        instance.gi.clickIdElement(Marker.LOGIN_TAB_ID);
        instance.gi.typeByIdText(Marker.USERNAME_LOGIN_ID, instance.universeEntity.login);
        instance.gi.typeByIdText(Marker.PASSWORD_LOGIN_ID, instance.universeEntity.pass);
        instance.gi.clickIdElement(Marker.LOGIN_SUBMIT_ID);

        //chooseServer
        instance.gi.clickTextIfExists(Text.AGREE);
        instance.gi.clickText(Test.PLAY_TEXT);
        instance.gi.doubleClickText(instance.universeEntity.name);

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
