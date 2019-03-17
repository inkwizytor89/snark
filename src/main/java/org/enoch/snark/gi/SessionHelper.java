package org.enoch.snark.gi;

import org.enoch.snark.instance.Instance;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.List;
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

    public void skipBannersIfExists() {
        try {
            final WebElement exitButton = webDriver.findElement(By.linkText("x"));
            exitButton.click();
        } catch (NoSuchElementException e) {}
        closeAgreement();
    }

    public void insertLoginData(String username, String password) {
        String loginTab = "ui-id-1";
        webDriver.findElement(By.id(loginTab)).click();
        session.sleep(TimeUnit.SECONDS, 1);
        webDriver.findElement(By.id("usernameLogin")).sendKeys(username);
        webDriver.findElement(By.id("passwordLogin")).sendKeys(password);
        final WebElement loginSubmit = webDriver.findElement(By.id("loginSubmit"));
        loginSubmit.submit();
    }

    public void chooseServer(String server) {
//        webDriver.findElement(By.className("btn btn-primary")).click();
//        webDriver.findElement(By.name(server)).click();
//        webDriver.findElement(By.name(server)).click();
        closeAgreement();
        try {
            webDriver.findElement(By.linkText("Graj")).click();
        } catch (NoSuchElementException e) {}
        WebElement serverElement = ((ChromeDriver) webDriver).findElementsByXPath("//*[contains(text(), '" + server + "')]").get(0);
        Actions actions = new Actions(webDriver);
        actions.doubleClick(serverElement).perform();
    }

    private void closeAgreement() {
        try {
            webDriver.findElement(By.linkText("Zgadzam się")).click();
        } catch (NoSuchElementException e) {}
    }
}
