package org.enoch.snark.gi;

import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.instance.Instance;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.concurrent.TimeUnit;

import static org.enoch.snark.instance.PropertyNames.WEBDRIVER_CHROME_DRIVER;

public class GISession {

    private final WebDriver webDriver;
    private final Instance instance;
    private final SessionHelper sessionHelper;

    private boolean isLoggedIn = false;


    public GISession(Instance instance) {
        this.instance = instance;
        System.setProperty(WEBDRIVER_CHROME_DRIVER, "C:/Program Files (x86)/Google/Chrome/chromedriver.exe");
        webDriver = new ChromeDriver();
        sessionHelper = new SessionHelper(instance, this);

        boolean islogged = false;
        while(!islogged) {
            try {
                open();
                islogged = true;
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    public void open() {
        webDriver.get(instance.universeEntity.getUrl());
        logIn();
    }

    public void close() {
        logOut();
//        seleniumDriver.close();
//        webDriver.quit();
    }

    private void logIn() {
        sessionHelper.skipBannerIfExists();
        sessionHelper.insertLoginData(
                instance.universeEntity.getLogin(),
                instance.universeEntity.getPass(),
                instance.universeEntity.getName()
        );
        isLoggedIn = true;
        sleep(TimeUnit.SECONDS, 1);
    }

    private void logOut() {
        new GIUrlBuilder(instance).openOverview();
        webDriver.findElement(By.linkText("Wyloguj")).click();
        isLoggedIn = false;
    }

    public void sleep(TimeUnit timeUnit, int i) {
        try {
            timeUnit.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public WebDriver getWebDriver() {
        return webDriver;
    }

}
