package org.enoch.snark.gi;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.exception.NotEnoughResources;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.exception.FleetCantStart;
import org.enoch.snark.instance.model.exception.ToStrongPlayerException;
import org.enoch.snark.instance.model.types.ResourceType;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.enoch.snark.instance.model.to.Resources.everything;
import static org.enoch.snark.instance.model.to.Resources.nothing;
import static org.enoch.snark.instance.model.types.ResourceType.DEUTERIUM;
import static org.enoch.snark.instance.model.uc.ResourceUC.*;
import static org.enoch.snark.instance.si.module.ConfigMap.LEAVE_MIN_RESOURCES;

public class SendFleetGIR extends GraphicalInterfaceReader {

    public void sendFleet(FleetEntity fleet) {
        SleepUtil.pause();
        final WebElement sendFleet = wd.findElement(By.id("sendFleet"));
        try {
            new WebDriverWait(wd, Duration.ofSeconds(2))
                    .until(ExpectedConditions.attributeContains(sendFleet, CLASS_ATTRIBUTE, "on"));
        } catch (TimeoutException e) {
            throw new FleetCantStart();
        }
        SleepUtil.sleep();
        sendFleet.click();

        WebElement errorBox = getIfPresentById("errorBoxDecision");
        if(errorBox != null && Mission.COLONIZATION.equals(fleet.mission)) {
            wd.findElement(By.id("errorBoxDecisionYes")).click();
        } else if(errorBox != null) {
            //todo change old code
            System.err.println("silny gracz ");
            throw new ToStrongPlayerException();
        }
    }

    public void setResources(Resources resources, ColonyEntity source) {
        if(resources == null || nothing.equals(resources)) return;
        if(everything.equals(resources)) selectAllResources();
        else setCustomResources(resources, source);
    }

    public void setNewResources(FleetPromise promise) {
        if(isNothingOrNull(promise.getResources())) return;
        if(isEverything(promise.getResources()) && isNothingOrNull(promise.getLeaveResources())) selectAllResources();
        else setNewCustomResources(promise);
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

    private void setNewCustomResources(FleetPromise promise) {
        WebElement resourcesArea = wd.findElement(By.id("resources"));
        WebElement metalInput = resourcesArea.findElement(By.xpath("//input[@id='metal']"));
        WebElement crystalInput = resourcesArea.findElement(By.xpath("//input[@id='crystal']"));
        WebElement deuteriumInput = resourcesArea.findElement(By.xpath("//input[@id='deuterium']"));

//        Resources finalLeave = readTransportConsumption().plus(promise.getLeaveResources());
        Resources transport = toTransport(promise);
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
            SleepUtil.sleep();
            WebElement element = wd.findElement(By.className("steps"));
            List<WebElement> steps = element.findElements(By.className("step"));
            WebElement speedElement = steps.get(Integer.parseInt(speed.toString()) / 10 - 1);
            speedElement.click();
        }
    }

    public void selectAllShips() {
        wd.findElement(By.id("sendall")).click();
    }

    public LocalTime parseDurationSecounds() {
        final String durationString = GI.getInstance().findElement("span", "id", "duration", "").getText();
        //Text '' could not be parsed at index 0 - popular error, shoud wait for not null time
        return DateUtil.parseDuration(durationString);
    }

    public LocalDateTime parseFleetVisited() {
        return parseDate("arrivalTime");
    }

    public LocalDateTime parseFleetBack() {
        return parseDate("returnTime");
    }

    private LocalDateTime parseDate(String dateId) {
        String dateString = wd.findElement(By.id(dateId)).getText();
        if(dateString.contains("-")) {
            SleepUtil.sleep();
            dateString = wd.findElement(By.id(dateId)).getText();
        }
        return DateUtil.parseToLocalDateTime(dateString);
    }
}
