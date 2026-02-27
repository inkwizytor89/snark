package org.enoch.snark.instance.si.module.consumer.gi;

import org.enoch.snark.action.command.SendCommand;
import org.enoch.snark.action.command.status.ExecutionIssue;
import org.enoch.snark.common.DateUtil;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.instance.model.exception.ShipDoNotExists;
import org.enoch.snark.instance.model.technology.Ship;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.enoch.snark.instance.model.to.Resources;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.enoch.snark.action.command.status.ExecutionIssue.*;
import static org.enoch.snark.instance.model.to.ShipsMap.ALL_SHIPS;
import static org.enoch.snark.instance.model.uc.ResourceUC.*;

public class SendFleetGIR extends GraphicalInterfaceReader {

    public SendFleetGIR(Wd wd) {
        super(wd);
    }

    @Deprecated
    public void sendFleetDeprecated(FleetEntity fleet) {
        sendFleet(fleet);
    }
    public ExecutionIssue sendFleet(FleetEntity fleet) {

        if(toWeakPlayer()) return TO_WEAK_PLAYER;
        SleepUtil.pause();
        final WebElement sendFleet = wd().findElement(By.id("sendFleet"));
        try {
            new WebDriverWait(wd(), Duration.ofSeconds(2))
                    .until(ExpectedConditions.attributeContains(sendFleet, CLASS_ATTRIBUTE, "on"));
        } catch (TimeoutException e) {
            return CAN_NOT_SENT;
        }
        SleepUtil.pause();
        sendFleet.click();

        WebElement errorBox = getIfPresentById("errorBoxDecision");
        if(errorBox != null && Mission.COLONIZATION.equals(fleet.mission)) {
            wd().findElement(By.id("errorBoxDecisionYes")).click();
            return NO_ISSUE;
        } else if(errorBox != null) {
            //todo change old code
            System.err.println("silny gracz ");
            return TO_STRONG_PLAYER;
        }
        return NO_ISSUE;
    }

    public void setNewResources(SendCommand command) {
        if(isNothingOrNull(command.getResources())) return;
        if(isEverything(command.getResources()) && isNothingOrNull(command.getLeaveResources())) selectAllResources();
        else setNewCustomResources(command.getSource(), command.getResources(), command.getLeaveResources());
    }

    private void setNewCustomResources(ColonyEntity source, Resources resources, Resources leave) {
        WebElement resourcesArea = wd().findElement(By.id("resources"));
        WebElement metalInput = resourcesArea.findElement(By.xpath("//input[@id='metal']"));
        WebElement crystalInput = resourcesArea.findElement(By.xpath("//input[@id='crystal']"));
        WebElement deuteriumInput = resourcesArea.findElement(By.xpath("//input[@id='deuterium']"));

        Resources finalLeave = readTransportConsumption().plus(leave);
        Resources transport = toTransport(source, resources, finalLeave);
        // tu sie zaczyna problem bo jak akcja zostanie powrórzona po błedzie to dalej bedzie problem bo dalej warunek jest spełniony, a przy konsumpci juz nie bedzie
//        if(transport == null) throw new NotEnoughResources("SendFleet.validateResources not fit for promise "+promise);

        for(int i=0; i<3; i++) {
            SleepUtil.pause();
            deuteriumInput.sendKeys(transport.deuterium.toString());
            crystalInput.sendKeys(transport.crystal.toString());
            metalInput.sendKeys(transport.metal.toString());

            long remainingResources = Long.parseLong(wd().findElement(By.id("remainingresources")).getText().replace(".", ""));
            long maxResources = Long.parseLong(wd().findElement(By.id("maxresources")).getText().replace(".", ""));

            if(remainingResources < maxResources)
                break;
        }
    }

    private void selectAllResources() {
        wd().findElement(By.id("allresources")).findElement(By.tagName("img")).click();
    }

    private Resources readTransportConsumption() {
        String consumptionInput = wd().findElement(By.id("consumption")).getText().trim();
        return new Resources(0L, 0L, toLong(consumptionInput.split("\\s")[0]));
    }

    public void setSpeed(Long speed) {
        if(speed != null) {
            WebElement element = wd().findElement(By.className("steps"));
            List<WebElement> steps = element.findElements(By.className("step"));
            WebElement speedElement = steps.get(Integer.parseInt(speed.toString()) / 10 - 1);
            speedElement.click();
        }
    }

    public ShipsMap selectShips(ShipsMap shipsMap) {
        //Scroll down till the bottom of the page
        ((JavascriptExecutor) wd.getWebDriver()).executeScript("window.scrollBy(0,document.body.scrollHeight)");
        if(ALL_SHIPS.equals(shipsMap)) return selectAllShips();
        else return selectRealShips(shipsMap);
    }

    public ShipsMap selectAllShips() {
        wd().findElement(By.id("sendall")).click();
        return ALL_SHIPS;
    }

    private ShipsMap selectRealShips(ShipsMap shipsMap) {
        ShipsMap taken = new ShipsMap();
        for (Map.Entry<Ship, Long> entry : shipsMap.entrySet()) {
            Long value = typeShip(entry.getKey(), entry.getValue());
            taken.put(entry.getKey(), value);
        }
        return taken;
    }

    private Long typeShip(Ship ship, Long count) {
        WebElement element = wd().findElement(By.name(ship.name()));
        if(!element.isEnabled()) {
            throw new ShipDoNotExists("Missing ships " + ship.name());
        }
        element.sendKeys(count.toString());
        return count;
    }

    public void next() {
        SleepUtil.pause();
        wd().findElement(By.className("planet-header")).click();

        final WebElement continueButton = wd().findElement(By.id("continueToFleet2"));
        if(continueButton.getAttribute("class").equals("continue off")) {
            throw new ShipDoNotExists("Can not select ships");
        }
        Actions actions = new Actions(wd());
        actions.moveToElement(continueButton).click().perform();
        SleepUtil.sleep();
    }

    public LocalTime parseDurationSecounds() {
        final String durationString = wd.findElement("span", "id", "duration", "").getText();
        //Text '' could not be parsed at index 0 - popular error, shoud wait for not null time
        return DateUtil.parseDuration(durationString);
    }

    public LocalDateTime parseFleetVisited() {
        return parseDate("arrivalTime");
    }

    public LocalDateTime parseFleetBack() {
        return parseDate("returnTime");
    }

    public boolean toWeakPlayer() {
        return wd().findElements(By.className("status_abbr_noob")).size() != 0;
    }

    private LocalDateTime parseDate(String dateId) {
        String dateString = wd().findElement(By.id(dateId)).getText();
        if(dateString.contains("-")) {
            SleepUtil.pause();
            dateString = wd().findElement(By.id(dateId)).getText();
        }
        return DateUtil.parseToLocalDateTime(dateString);
    }

    public void fixDoNotWorkingDefaults(SendCommand command) {
        // spy on position 16
        if(Mission.SPY.equals(command.getMission()) && command.getTarget().getPlanet().position == 16) {
            wd().findElement(By.id("missionButton6")).click();
        }
    }
}
