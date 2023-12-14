package org.enoch.snark.gi;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.Resources;
import org.enoch.snark.model.exception.FleetCantStart;
import org.enoch.snark.model.exception.ToStrongPlayerException;
import org.enoch.snark.model.types.ResourceType;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static org.enoch.snark.module.ConfigMap.LEAVE_MIN_RESOURCES;

public class SendFleetGIR extends GraphicalInterfaceReader {

    public void sendFleet(FleetEntity fleet) {
        SleepUtil.pause();
        final WebElement sendFleet = wd.findElement(By.id("sendFleet"));
        try {
            new WebDriverWait(wd, 2)
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

    public void setResources(FleetEntity fleet) {
        if(fleet.metal != null || fleet.crystal != null || fleet.deuterium != null) {
            WebElement resourcesArea = wd.findElement(By.id("resources"));
            WebElement metalAmount = resourcesArea.findElement(By.xpath("//input[@id='metal']"));
            WebElement crystalAmount = resourcesArea.findElement(By.xpath("//input[@id='crystal']"));
            WebElement deuteriumAmount = resourcesArea.findElement(By.xpath("//input[@id='deuterium']"));

            Resources defaultResources = Instance.getMainConfigMap().getConfigResource(LEAVE_MIN_RESOURCES, new Resources("d4m"));
            Long metal = rememberToLeaveSome(fleet, defaultResources.metal, ResourceType.METAL);
            Long crystal = rememberToLeaveSome(fleet, defaultResources.crystal, ResourceType.CRYSTAL);
            Long deuterium = rememberToLeaveSome(fleet, defaultResources.deuterium, ResourceType.DEUTERIUM);
            for(int i=0; i<3; i++) {
                SleepUtil.pause();
                deuteriumAmount.sendKeys(deuterium.toString());
                crystalAmount.sendKeys(crystal.toString());
                metalAmount.sendKeys(metal.toString());

                long remainingresources = Long.parseLong(wd.findElement(By.id("remainingresources")).getText().replace(".", ""));
                long maxresources = Long.parseLong(wd.findElement(By.id("maxresources")).getText().replace(".", ""));

                if(remainingresources < maxresources )
                    break;
            }
        }
    }

    public Long rememberToLeaveSome(FleetEntity fleet, Long toLeave, ResourceType resource) {
        switch(resource) {
            case METAL:{
                return Math.min(fleet.metal, fleet.source.metal) - toLeave;
            }
            case CRYSTAL:{
                return Math.min(fleet.crystal, fleet.source.crystal) - toLeave;
            }
            case DEUTERIUM:{
                String consumptionInput = wd.findElement(By.id("consumption")).getText().trim();
                long consumption = toLong(consumptionInput.split("\\s")[0]);

                long l = 4000000L;
                return Math.min(fleet.deuterium, fleet.source.deuterium) - consumption - toLeave;
            }
        }
        throw new IllegalStateException("Unknown resource "+resource.name());
    }

    public void setSpeed(FleetEntity fleet) {
        if(fleet.speed != null) {
            SleepUtil.sleep();
            WebElement element = wd.findElement(By.className("steps"));
            List<WebElement> steps = element.findElements(By.className("step"));
            WebElement speedElement = steps.get(Integer.parseInt(fleet.speed.toString()) / 10 - 1);
            speedElement.click();
        }
    }

    public void selectAllShips() {
        wd.findElement(By.id("sendall")).click();
    }

    public void selectAllResources() {
        wd.findElement(By.id("allresources")).findElement(By.tagName("img")).click();
    }
}
