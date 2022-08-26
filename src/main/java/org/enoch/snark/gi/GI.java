package org.enoch.snark.gi;

import org.enoch.snark.exception.GIException;
import org.enoch.snark.instance.Utils;
import org.enoch.snark.model.EventFleet;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.enoch.snark.instance.PropertyNames.WEBDRIVER_CHROME_DRIVER;

public class GI {

    public static final String TR = "tr";
    public static final String HREF_ATTRIBUTE = "href";
    public static final String TITLE_ATTRIBUTE = "title";
    public final WebDriver webDriver;

    public GI() {
        System.setProperty(WEBDRIVER_CHROME_DRIVER, "C:/Program Files (x86)/Google/Chrome/chromedriver.exe");
        webDriver = new ChromeDriver();
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

    public WebElement findElement(String tag, String attribute, String value, String unacceptable) {
        WebElement element = findByXPath("//" + tag + "[@" + attribute + "='" + value + "']");
        for (int i = 1; i <= 10; i++) {
             element = findByXPath("//" + tag + "[@" + attribute + "='" + value + "']");
             if(!unacceptable.equals(element.getText())) {
                 break;
             }
            sleep(TimeUnit.MILLISECONDS, 200 * i);
        }
        if(unacceptable.equals(element.getText())) {
            throw new GIException(GIException.NOT_FOUND, "//"+tag+"[@" + attribute + "='"+value+"']",
                    "Found " + element.getText() + " but unacceptable was "+ unacceptable);
        }
        return element;
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

    public List<EventFleet> readEventFleet() {
        List<EventFleet> eventFleets = new ArrayList<>();
        try {
            List<WebElement> eventHeader = webDriver.findElements(By.id("eventHeader"));
            if(!eventHeader.isEmpty()) {
                if(eventHeader.get(0).isDisplayed())
                    webDriver.findElement(By.className("event_list")).click();
            }
            Utils.sleep();
            webDriver.findElement(By.className("event_list")).click();
            Utils.sleep();
            List<WebElement> tableRows = new WebDriverWait(webDriver, 5)
                            .until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(webDriver.findElement(By.id("eventContent")), By.tagName(TR)));

            for (WebElement webElement : tableRows) {
                EventFleet eventFleet = new EventFleet();
                WebElement countDown = webElement.findElement(By.className("countDown"));
                List<WebElement> hostile = countDown.findElements(By.className("hostile"));
                eventFleet.isForeign = !hostile.isEmpty();
                eventFleet.countDown =  countDown.getText();
                eventFleet.arrivalTime =  webElement.findElement(By.className("arrivalTime")).getText();
                eventFleet.arrivalTime =  webElement.findElement(By.className("arrivalTime")).getText();
                eventFleet.missionFleet =  webElement.findElement(By.className("missionFleet")).findElement(By.className("tooltipHTML")).getAttribute(TITLE_ATTRIBUTE);
                eventFleet.originFleet =  webElement.findElement(By.className("originFleet")).getText();
                eventFleet.coordsOrigin =  webElement.findElement(By.className("coordsOrigin")).getText();
                eventFleet.detailsFleet =  webElement.findElement(By.className("detailsFleet")).getText();
                eventFleet.iconMovement =  "?";
                eventFleet.destFleet =  webElement.findElement(By.className("destFleet")).getText();
                eventFleet.destCoords =  webElement.findElement(By.className("destCoords")).getText();
                eventFleet.sendProbe =  webElement.findElement(By.className("sendProbe")).getText();
                List<WebElement> player = webElement.findElements(By.className("sendMail"));
                if(player.size() > 1) {
                    eventFleet.sendMail =  player.get(1).getAttribute(HREF_ATTRIBUTE);
                } else {
                    eventFleet.sendMail = "";
                }

                eventFleets.add(eventFleet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return eventFleets;
    }

    public void sleep(TimeUnit timeUnit, int i) {
        try {
            timeUnit.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
