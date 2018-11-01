package org.enoch.snark.gi;

import org.enoch.snark.instance.Instance;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.concurrent.TimeUnit;

class SessionHelper {
    private Instance instance;
    private final GISession session;
    private final WebDriver webDriver;

    SessionHelper(Instance instance, GISession session) {
        this.instance = instance;
        this.session = session;
        this.webDriver = session.getWebDriver();

        webDriver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        webDriver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
        webDriver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
    }

    public void skipBannerIfExists() {
        try {
            final WebElement exitButton = webDriver.findElement(By.linkText("x"));
            exitButton.click();
        } catch (NoSuchElementException e) {}
    }

    public void insertLoginData(String username, String password, String server) {
        final WebElement loginButton = webDriver.findElement(By.id("loginBtn"));
        loginButton.click();

        session.sleep(TimeUnit.SECONDS, 1);
        webDriver.findElement(By.id("usernameLogin")).sendKeys(username);
        session.sleep(TimeUnit.SECONDS, 1);
        webDriver.findElement(By.id("passwordLogin")).sendKeys(password);
        session.sleep(TimeUnit.SECONDS, 1);
        webDriver.findElement(By.id("serverLogin")).sendKeys(server);

        final WebElement loginSubmit = webDriver.findElement(By.id("loginSubmit"));
        session.sleep(TimeUnit.SECONDS, 1);
        loginSubmit.click();
    }
}
