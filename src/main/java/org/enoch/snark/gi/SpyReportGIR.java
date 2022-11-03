package org.enoch.snark.gi;

import org.enoch.snark.db.entity.TargetEntity;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class SpyReportGIR extends GraphicalInterfaceReader {

    public TargetEntity readTargetFromReport(String link) {
        wd.get(link);

        boolean increaseSpy = false;

        String msgTitle = wd.findElement(By.className("msg_title")).getText();
        TargetEntity target = new TargetEntity(extractCoordinateFromTitle(msgTitle));// type moon-planet missing

        WebElement resourceSection = wd.findElementsByXPath("//ul[@data-type='resources']").get(0);
        List<WebElement> resourceList = resourceSection.findElements(By.className("resource_list_el"));
        target.metal = toLong(resourceList.get(0).getAttribute("title"));
        target.crystal = toLong(resourceList.get(1).getAttribute("title"));
        target.deuterium = toLong(resourceList.get(2).getAttribute("title"));
        target.energy = toLong(resourceList.get(3).getAttribute("title"));

        WebElement fleetSection = wd.findElementsByXPath("//ul[@data-type='ships']").get(0);
        List<WebElement> toLowSpyLevel = fleetSection.findElements(By.className("detail_list_fail"));
        if(!toLowSpyLevel.isEmpty()) {
            increaseSpy = true;
        } else {
            List<WebElement> fleetList = fleetSection.findElements(By.className("detail_list_el"));
            if (fleetList.isEmpty()) {
                target.fleetSum = 0L;
            } else {
                for (WebElement fleetPosition : fleetList) {
                    String code = fleetPosition.findElement(By.tagName("img")).getAttribute("class");
                    if (code.contains("tech204")) target.fighterLight = toLong(fleetPosition.findElement(By.className("fright")).getText());
                    else if (code.contains("tech205")) target.fighterHeavy = toLong(fleetPosition.findElement(By.className("fright")).getText());
                    else if (code.contains("tech206")) target.cruiser = toLong(fleetPosition.findElement(By.className("fright")).getText());
                    else if (code.contains("tech207")) target.battleship = toLong(fleetPosition.findElement(By.className("fright")).getText());
                    else if (code.contains("tech215")) target.interceptor = toLong(fleetPosition.findElement(By.className("fright")).getText());
                    else if (code.contains("tech211")) target.bomber = toLong(fleetPosition.findElement(By.className("fright")).getText());
                    else if (code.contains("tech213")) target.destroyer = toLong(fleetPosition.findElement(By.className("fright")).getText());
                    else if (code.contains("tech214")) target.deathstar = toLong(fleetPosition.findElement(By.className("fright")).getText());
                    else if (code.contains("tech218")) target.reaper = toLong(fleetPosition.findElement(By.className("fright")).getText());
                    else if (code.contains("tech219")) target.explorer = toLong(fleetPosition.findElement(By.className("fright")).getText());
                    else if (code.contains("tech202")) target.transporterSmall = toLong(fleetPosition.findElement(By.className("fright")).getText());
                    else if (code.contains("tech203")) target.transporterLarge = toLong(fleetPosition.findElement(By.className("fright")).getText());
                    else if (code.contains("tech208")) target.colonyShip = toLong(fleetPosition.findElement(By.className("fright")).getText());
                    else if (code.contains("tech209")) target.recycler = toLong(fleetPosition.findElement(By.className("fright")).getText());
                    else if (code.contains("tech210")) target.espionageProbe = toLong(fleetPosition.findElement(By.className("fright")).getText());
                }
            }
        }

        WebElement defenseSection = wd.findElementsByXPath("//ul[@data-type='ships']").get(0);
        toLowSpyLevel = defenseSection.findElements(By.className("detail_list_fail"));
        if(!toLowSpyLevel.isEmpty()) {
            increaseSpy = true;
        } else {
            List<WebElement> fleetList = fleetSection.findElements(By.className("resource_list_el"));
            if (fleetList.isEmpty()) {
                target.defenseSum = 0L;
            } else {
                for (WebElement defensePosition : fleetList) {
                    String code = defensePosition.findElement(By.tagName("img")).getAttribute("class");
                    if (code.contains("defense401")) target.rocketLauncher = toLong(defensePosition.findElement(By.className("fright")).getText());
                    else if (code.contains("defense402")) target.laserCannonLight = toLong(defensePosition.findElement(By.className("fright")).getText());
                    else if (code.contains("defense403")) target.cruiser = toLong(defensePosition.findElement(By.className("fright")).getText());
                    else if (code.contains("defense404")) target.gaussCannon = toLong(defensePosition.findElement(By.className("fright")).getText());
                    else if (code.contains("defense405")) target.ionCannon = toLong(defensePosition.findElement(By.className("fright")).getText());
                    else if (code.contains("defense406")) target.plasmaCannon = toLong(defensePosition.findElement(By.className("fright")).getText());
                    else if (code.contains("defense407")) target.shieldDomeSmall = toLong(defensePosition.findElement(By.className("fright")).getText());
                    else if (code.contains("defense408")) target.shieldDomeLarge = toLong(defensePosition.findElement(By.className("fright")).getText());
                    else if (code.contains("defense502")) target.missileInterceptor = toLong(defensePosition.findElement(By.className("fright")).getText());
                    else if (code.contains("defense503")) target.missileInterplanetary = toLong(defensePosition.findElement(By.className("fright")).getText());
                }
            }
        }
        if(increaseSpy) {
            target.spyLevel = target.spyLevel * 2;
        }

        return target;
    }


    private String extractCoordinateFromTitle(String input) {
        final String[] inputParts = input.split("\\s+");
        return inputParts[inputParts.length-1];
    }
}
