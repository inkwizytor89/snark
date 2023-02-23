package org.enoch.snark.gi;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.model.exception.FleetCantStart;
import org.enoch.snark.model.exception.ToStrongPlayerException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class SendFleetGIR extends GraphicalInterfaceReader {

    public void sendFleet(FleetEntity fleet) {
        SleepUtil.pause();
        final WebElement sendFleet = wd.findElement(By.id("sendFleet"));
        Boolean isExpectedState = new WebDriverWait(wd, 2)
                .until(ExpectedConditions.attributeContains(sendFleet, CLASS_ATTRIBUTE, "on"));
        if(!isExpectedState) {
            throw new FleetCantStart();
        }
        SleepUtil.pause();
        sendFleet.click();

        WebElement errorBox = getIfPresentById("errorBoxDecision");
        if(errorBox != null && Mission.COLONIZATION.name().equals(fleet.type)) {
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
            for(int i=0; i<3; i++) {
                SleepUtil.pause();
                deuteriumAmount.sendKeys(fleet.deuterium.toString());
                crystalAmount.sendKeys(fleet.crystal.toString());
                metalAmount.sendKeys(fleet.metal.toString());

                long remainingresources = Long.parseLong(wd.findElement(By.id("remainingresources")).getText().replace(".", ""));
                long maxresources = Long.parseLong(wd.findElement(By.id("maxresources")).getText().replace(".", ""));

                if(remainingresources < maxresources )
                    break;
            }
        }
    }

    public void setSpeed(FleetEntity fleet) {
        if(fleet.speed != null) {
            WebElement element = wd.findElement(By.className("steps"));
            List<WebElement> steps = element.findElements(By.className("step"));
            WebElement speedElement = steps.get(Integer.parseInt(fleet.speed.toString()) / 10 - 1);
            speedElement.click();
        }
    }
}
