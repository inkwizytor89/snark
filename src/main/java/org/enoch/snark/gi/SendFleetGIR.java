package org.enoch.snark.gi;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class SendFleetGIR extends GraphicalInterfaceReader {

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
