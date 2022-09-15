package org.enoch.snark.gi.macro;

import org.enoch.snark.gi.GISession;
import org.enoch.snark.model.exception.PlanetDoNotExistException;
import org.enoch.snark.model.exception.ShipDoNotExists;
import org.enoch.snark.model.exception.ToStrongPlayerException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.concurrent.TimeUnit;

public class FleetSelector {

    private final WebDriver webDriver;
    private final GISession session;

    public FleetSelector(GISession session){
        this.session = session;
        webDriver = session.getWebDriver();
    }

    public void typeShip(ShipEnum shipEnum, Long count) {
        WebElement element = webDriver.findElement(By.name(shipEnum.getId()));
        if(!element.isEnabled()) {
            throw new ShipDoNotExists("Missings ships " + shipEnum.getId());
        }
        element.sendKeys(count.toString());
    }

    public void next() {
        session.gi.sleep(TimeUnit.SECONDS, 1);
        // to button continue to recalculate
        session.getWebDriver().findElement(By.className("planet-header")).click();

        final WebElement continueButton = session.getWebDriver().findElement(By.id("continueToFleet2"));
        if(continueButton.getAttribute("class").equals("continue off"))
            throw new ShipDoNotExists();
//        continueButton.click();


        Actions actions = new Actions(session.getWebDriver());

        actions.moveToElement(continueButton).click().perform();
    }

    public boolean start() {
        session.gi.sleep(TimeUnit.SECONDS, 2);
        final WebElement startInput = session.getWebDriver().findElement(By.id("sendFleet"));
        if(startInput.getTagName().equals("td")) {
            throw new PlanetDoNotExistException();
        }
        startInput.click();
        session.gi.sleep(TimeUnit.SECONDS, 1);
        final WebElement errorBoxDecisionNo = session.getWebDriver().findElement(By.id("errorBoxDecisionNo"));
        if(errorBoxDecisionNo.isDisplayed()) {
            throw new ToStrongPlayerException();
        }
        return true;
    }
}
