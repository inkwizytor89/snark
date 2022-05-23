package org.enoch.snark.gi;

import org.enoch.snark.exception.GIException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.enoch.snark.instance.PropertyNames.WEBDRIVER_CHROME_DRIVER;

public class GI {

    public final WebDriver webDriver;

    public GI() {
        System.setProperty(WEBDRIVER_CHROME_DRIVER, "C:/Program Files (x86)/Google/Chrome/chromedriver.exe");
        webDriver = new ChromeDriver();
    }

    public void typeByIdText(String elementById, String text) {
        WebElement element = findElement(By.id(elementById));
        element.sendKeys(text);
    }

    public void typeByNameText(String name, String text) {
        WebElement element = findElement(By.name(name));
        element.sendKeys(text);
    }

    public void clickText(String text) {
        try {
            WebElement textToClick = findElement(By.linkText(text));
            textToClick.click();
        } catch (NoSuchElementException e) {
            System.err.println("Skip text '" + text + "' to click");
        }
    }

    public void clickTextIfExists(String text) {
        try {
            WebElement textToClick = findElementIfExists(By.linkText(text));
            if(textToClick != null) {
                textToClick.click();
            }
        } catch (NoSuchElementException e) {
            System.err.println("Skip text '" + text + "' to click");
        }
    }

    public void clickIdElement(String elementById) {
        try {
            WebElement elementToClick = findElement(By.id(elementById));
            elementToClick.click();
        } catch (NoSuchElementException e) {
            System.err.println("Skip elementById '" + elementById + "' to click");
        }
    }

    public void doubleClickText(String text) {
        try {
            WebElement serverElement = findTextByXPath(text);
            Actions actions = new Actions(webDriver);
            actions.doubleClick(serverElement).perform();
        } catch (NoSuchElementException e) {
            System.err.println("Skip text '" + text + "' to click");
        }
    }

    private WebElement findElement(By by) {
        return findElement(by, 10);
    }

    private WebElement findElementIfExists(By by) {
        return findElement(by, 0);
    }

    private WebElement findTextByXPath(String text) {
        List<WebElement> elements = ((ChromeDriver) webDriver).findElementsByXPath("//*[contains(text(), '" + text + "')]");
        int i = 1;
        while(elements.isEmpty() && i <= 10) {
            sleep(TimeUnit.MILLISECONDS, 200 * i);
            i++;
            elements = ((ChromeDriver) webDriver).findElementsByXPath("//*[contains(text(), '" + text + "')]");
        }

        if(elements.isEmpty()) {
            System.err.println("No element '" + text + "' to click");
            return ((ChromeDriver) webDriver).findElementsByXPath("//*[contains(text(), '" + text + "')]").get(0);
        } else if(elements.size() > 1) {
            System.err.println("Were many elements '" + text + "' to click");
        }
        return elements.get(0);
    }

    public WebElement findButtonContainsText(String text) {
        return findByXPath("//button[contains(text(), '" + text + "')]");
    }

    public WebElement findElement(String tag, String attribute, String value) {
        return findByXPath("//"+tag+"[@" + attribute + "='"+value+"']");
    }

    public WebElement findText(String text) {
        return findByXPath("//*[contains(text(), '" + text + "')]");
    }

    private WebElement findByXPath(String using) {
        List<WebElement> elements = ((ChromeDriver) webDriver).findElementsByXPath(using);
        int i = 1;
        while(elements.isEmpty() && i <= 10) {
            sleep(TimeUnit.MILLISECONDS, 200 * i);
            i++;
            elements = ((ChromeDriver) webDriver).findElementsByXPath(using);
        }

        if(elements.isEmpty()) {
            throw new GIException(GIException.NOT_FOUND, using, "No element " + using);
        } else if(elements.size() > 1) {
            throw new GIException(GIException.TOO_MANY, using, "No element " + using);
        }
        return elements.get(0);
    }

    private WebElement findElement(By by, int toDelay) {
        List<WebElement> elements = webDriver.findElements(by);
        int i = 1;
        while(elements.isEmpty() && i <= toDelay) {
            sleep(TimeUnit.MILLISECONDS, 200 * i);
            i++;
            elements = webDriver.findElements(by);
        }

        if(elements.isEmpty()) {
            System.err.println("No element '" + by.toString() + "' to click");
            if(toDelay == 0 ) {
                return null;
            }
            return webDriver.findElement(by);
        } else if(elements.size() > 1) {
            System.err.println("Were many elements '" + by.toString() + "' to click");
        }
        return elements.get(0);
    }

    public void sleep(TimeUnit timeUnit, int i) {
        try {
            timeUnit.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
