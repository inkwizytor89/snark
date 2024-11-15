package org.enoch.snark.gi;

import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.model.technology.Building;
import org.enoch.snark.instance.model.technology.Defense;
import org.enoch.snark.instance.model.technology.Ship;
import org.enoch.snark.instance.model.types.ColonyType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpyReportGIR extends GraphicalInterfaceReader {

    public TargetEntity readTargetFromReport(String link) {
        wd.get(link);

        String msgTitle = wd.findElement(By.className("msg_title")).getText();
        TargetEntity target = new TargetEntity(extractCoordinateFromTitle(msgTitle));

        if(wd.findElements(By.className("researchSection")).isEmpty()) {
            return target;
        }
        target.lastSpiedOn = LocalDateTime.now();

        List<WebElement> resourceList = wd.findElement(By.className("resourceLootInfo")).findElements(By.tagName("resource-icon"));
        target.metal = extractLongFromAriaDescription(resourceList.get(0));
        target.crystal = extractLongFromAriaDescription(resourceList.get(1));
        target.deuterium = extractLongFromAriaDescription(resourceList.get(2));
        target.energy = extractLongFromAriaDescription(resourceList.get(5));

        if(target.metal == 0 && ColonyType.PLANET.equals(target.type))
            System.err.println("Potential error: spy and planet with no metal "+target.toPlanet());

        List<WebElement> fleetPositions = fetchPositionsInSection("fleetSection");
        if (!fleetPositions.isEmpty()) for(Ship ship : Ship.values()) {
            String key = ship.name().toLowerCase();
            Optional<WebElement> element = fleetPositions.stream().filter(value -> value.getAttribute(key)!= null).findFirst();
            element.ifPresent(webElement -> target.put(ship, extractLongFromAriaDescription(webElement)));
        }
        target.calculateShips();

        List<WebElement> defensePositions = fetchPositionsInSection("defenseSection");
        if (!defensePositions.isEmpty()) for(Defense defense : Defense.values()) {
            String key = defense.name().toLowerCase();
            Optional<WebElement> element = defensePositions.stream().filter(value -> value.getAttribute(key)!= null).findFirst();
            element.ifPresent(webElement -> target.put(defense, extractLongFromAriaDescription(webElement)));
        }
        target.calculateDefense();

        List<WebElement> buildingsPositions = fetchPositionsInSection("buildingsSection");
        if (!buildingsPositions.isEmpty()) for(Building building : Building.baseBuildings()) {
            String key = building.name().toLowerCase();
            Optional<WebElement> element = buildingsPositions.stream().filter(value -> value.getAttribute(key)!= null).findFirst();
            element.ifPresent(webElement -> target.put(building, extractLongFromAriaDescription(webElement)));
        }

        return target;
    }

    private List<WebElement> fetchPositionsInSection(String sectionName) {
        List<WebElement> sectionPositions = wd.findElements(By.className(sectionName));
        if (sectionPositions.isEmpty()) return new ArrayList<>();
        return sectionPositions.get(0).findElements(By.tagName("technology-icon"));
    }

    private Long extractLongFromAriaDescription(WebElement element) {
        String attributeString = element.getAttribute("aria-description");
        return Long.parseLong(attributeString.substring(attributeString.lastIndexOf(" ")).trim());
    }

    private String extractCoordinateFromTitle(String input) {
        String type = input.contains("lanet") ? "p" : "m";
        final String[] inputParts = input.split("\\s+");
        return type+inputParts[inputParts.length-1];
    }
}
