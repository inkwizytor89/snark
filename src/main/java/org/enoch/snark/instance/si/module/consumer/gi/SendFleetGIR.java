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
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.exception.FleetCantStart;
import org.enoch.snark.instance.model.exception.ToStrongPlayerException;
import org.enoch.snark.instance.model.types.ResourceType;
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
import static org.enoch.snark.instance.model.to.Resources.everything;
import static org.enoch.snark.instance.model.to.Resources.nothing;
import static org.enoch.snark.instance.model.to.ShipsMap.ALL_SHIPS;
import static org.enoch.snark.instance.model.types.ResourceType.DEUTERIUM;
import static org.enoch.snark.instance.model.uc.ResourceUC.*;
import static org.enoch.snark.instance.model.uc.ShipUC.fromExpressionToValues;
import static org.enoch.snark.instance.si.module.ThreadMap.LEAVE_MIN_RESOURCES;

public class SendFleetGIR extends GraphicalInterfaceReader {

    public SendFleetGIR(GI gi) {
        super(gi);
    }

    @Deprecated
    public void sendFleetDeprecated(FleetEntity fleet) {
        sendFleet(fleet);
    }
    public ExecutionIssue sendFleet(FleetEntity fleet) {

        if(toWeakPlayer()) return TO_WEAK_PLAYER;
        SleepUtil.pause();
        final WebElement sendFleet = wd.findElement(By.id("sendFleet"));
        try {
            new WebDriverWait(wd, Duration.ofSeconds(2))
                    .until(ExpectedConditions.attributeContains(sendFleet, CLASS_ATTRIBUTE, "on"));
        } catch (TimeoutException e) {
            return CAN_NOT_SENT;
        }
        SleepUtil.pause();
        sendFleet.click();

        WebElement errorBox = getIfPresentById("errorBoxDecision");
        if(errorBox != null && Mission.COLONIZATION.equals(fleet.mission)) {
            wd.findElement(By.id("errorBoxDecisionYes")).click();
            return NO_ISSUE;
        } else if(errorBox != null) {
            //todo change old code
            System.err.println("silny gracz ");
            return TO_STRONG_PLAYER;
        }
        return NO_ISSUE;
    }

    public void setResources(Resources resources, ColonyEntity source) {
        if(resources == null || nothing.equals(resources)) return;
        if(everything.equals(resources)) selectAllResources();
        else setCustomResources(resources, source);
    }

    public void setNewResources(SendCommand command) {
        if(isNothingOrNull(command.getResources())) return;
        if(isEverything(command.getResources()) && isNothingOrNull(command.getLeaveResources())) selectAllResources();
        else setNewCustomResources(command.getSource(), command.getResources(), command.getLeaveResources());
    }

    public void setNewResources(FleetPromise promise) {
        if(isNothingOrNull(promise.getResources())) return;
        if(isEverything(promise.getResources()) && isNothingOrNull(promise.getLeaveResources())) selectAllResources();
        else setNewCustomResources(promise.getSource(), promise.getResources(), promise.getLeaveResources());
    }

    private void setCustomResources(Resources resources, ColonyEntity source) {
        if(resources.metal != null || resources.crystal != null || resources.deuterium != null) {
            WebElement resourcesArea = wd.findElement(By.id("resources"));
            WebElement metalAmount = resourcesArea.findElement(By.xpath("//input[@id='metal']"));
            WebElement crystalAmount = resourcesArea.findElement(By.xpath("//input[@id='crystal']"));
            WebElement deuteriumAmount = resourcesArea.findElement(By.xpath("//input[@id='deuterium']"));

            Resources defaultResources = Instance.getGlobalMainConfigMap().getConfigResources(LEAVE_MIN_RESOURCES, new Resources("d4m"));
            Long metal = rememberToLeaveSome(resources, source, defaultResources.metal, ResourceType.METAL);
            Long crystal = rememberToLeaveSome(resources, source, defaultResources.crystal, ResourceType.CRYSTAL);
            Long deuterium = rememberToLeaveSome(resources, source, defaultResources.deuterium, DEUTERIUM);
            for(int i=0; i<3; i++) {
                SleepUtil.pause();
                deuteriumAmount.sendKeys(deuterium.toString());
                crystalAmount.sendKeys(crystal.toString());
                metalAmount.sendKeys(metal.toString());

                long remainingResources = Long.parseLong(wd.findElement(By.id("remainingresources")).getText().replace(".", ""));
                long maxResources = Long.parseLong(wd.findElement(By.id("maxresources")).getText().replace(".", ""));

                if(remainingResources < maxResources)
                    break;
            }
        }
    }

    private void setNewCustomResources(ColonyEntity source, Resources resources, Resources leave) {
        WebElement resourcesArea = wd.findElement(By.id("resources"));
        WebElement metalInput = resourcesArea.findElement(By.xpath("//input[@id='metal']"));
        WebElement crystalInput = resourcesArea.findElement(By.xpath("//input[@id='crystal']"));
        WebElement deuteriumInput = resourcesArea.findElement(By.xpath("//input[@id='deuterium']"));

//        Resources finalLeave = readTransportConsumption().plus(promise.getLeaveResources());
        Resources transport = toTransport(source, resources, leave);
        // tu sie zaczyna problem bo jak akcja zostanie powrórzona po błedzie to dalej bedzie problem bo dalej warunek jest spełniony, a przy konsumpci juz nie bedzie
//        if(transport == null) throw new NotEnoughResources("SendFleet.validateResources not fit for promise "+promise);

        for(int i=0; i<3; i++) {
            SleepUtil.pause();
            deuteriumInput.sendKeys(transport.deuterium.toString());
            crystalInput.sendKeys(transport.crystal.toString());
            metalInput.sendKeys(transport.metal.toString());

            long remainingResources = Long.parseLong(wd.findElement(By.id("remainingresources")).getText().replace(".", ""));
            long maxResources = Long.parseLong(wd.findElement(By.id("maxresources")).getText().replace(".", ""));

            if(remainingResources < maxResources)
                break;
        }
    }

    private void selectAllResources() {
        wd.findElement(By.id("allresources")).findElement(By.tagName("img")).click();
    }

    public Long rememberToLeaveSome(Resources resources, ColonyEntity source, Long toLeave, ResourceType resource) {
        switch(resource) {
            case METAL: return Math.max(resources.skipLeaveMetal ? resources.metal : resources.metal - toLeave, 0L);
            case CRYSTAL: return Math.max(resources.skipLeaveCrystal ? resources.crystal : resources.crystal - toLeave, 0L);
            case DEUTERIUM:{
                long consumption = readTransportDeuteriumConsumption();
                return Math.max(resources.skipLeaveDeuterium ? resources.deuterium : resources.deuterium - toLeave - consumption, 0L);
            }
        }
        throw new IllegalStateException("Unknown resource "+resource.name());
    }

    private long readTransportDeuteriumConsumption() {
        String consumptionInput = wd.findElement(By.id("consumption")).getText().trim();
        return toLong(consumptionInput.split("\\s")[0]);
    }

    private Resources readTransportConsumption() {
        String consumptionInput = wd.findElement(By.id("consumption")).getText().trim();
        return new Resources(0L, 0L, toLong(consumptionInput.split("\\s")[0]));
    }

    public void setSpeed(Long speed) {
        if(speed != null) {
            SleepUtil.pause();
            WebElement element = wd.findElement(By.className("steps"));
            List<WebElement> steps = element.findElements(By.className("step"));
            WebElement speedElement = steps.get(Integer.parseInt(speed.toString()) / 10 - 1);
            speedElement.click();
        }
    }

    public void selectShips(SendCommand command) {
        if(ALL_SHIPS.equals(command.getShipsMap()) && (command.getLeaveShipsMap() == null || command.getLeaveShipsMap().isEmpty())) {
            selectAllShips();
        } else {
            ShipsMap realCanToSend = fromExpressionToValues(command.getShipsMap(), command);
            selectShips(realCanToSend);
        }
        //Scroll down till the bottom of the page
        ((JavascriptExecutor) gi.getWebDriver()).executeScript("window.scrollBy(0,document.body.scrollHeight)");
    }

    public void selectAllShips() {
        wd.findElement(By.id("sendall")).click();
    }

    private void selectShips(ShipsMap shipsMap) {
        for (Map.Entry<Ship, Long> entry : shipsMap.entrySet()) {
            Long value = typeShip(entry.getKey(), entry.getValue());
//            promise.setShipsMap(ShipsMap.createSingle(entry.getKey(), value));
        }
    }

    private Long typeShip(Ship ship, Long count) {
        WebElement element = wd.findElement(By.name(ship.name()));
        if(!element.isEnabled()) {
            throw new ShipDoNotExists("Missing ships " + ship.name());
        }
        element.sendKeys(count.toString());
        return count;
    }

    public void next() {
        SleepUtil.pause();
        wd.findElement(By.className("planet-header")).click();

        final WebElement continueButton = wd.findElement(By.id("continueToFleet2"));
        if(continueButton.getAttribute("class").equals("continue off")) {
            throw new ShipDoNotExists("Can not select ships");
        }
        Actions actions = new Actions(wd);
        actions.moveToElement(continueButton).click().perform();
        SleepUtil.pause();
    }

    public LocalTime parseDurationSecounds() {
        final String durationString = gi.findElement("span", "id", "duration", "").getText();
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
        return wd.findElements(By.className("status_abbr_noob")).size() != 0;
    }

    private LocalDateTime parseDate(String dateId) {
        String dateString = wd.findElement(By.id(dateId)).getText();
        if(dateString.contains("-")) {
            SleepUtil.pause();
            dateString = wd.findElement(By.id(dateId)).getText();
        }
        return DateUtil.parseToLocalDateTime(dateString);
    }
}
