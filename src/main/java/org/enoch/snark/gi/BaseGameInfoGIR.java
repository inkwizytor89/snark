package org.enoch.snark.gi;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.types.GIUrl;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.types.ColonyType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.enoch.snark.gi.types.UrlComponent.*;

public class BaseGameInfoGIR extends GraphicalInterfaceReader {

    public List<ColonyEntity> loadPlanetList() {
        ArrayList<ColonyEntity> colonyEntities = new ArrayList<>();

        WebElement myPlanets = wd.findElement(By.id("myPlanets"));
        List<WebElement> coloniesWebElements = new WebDriverWait(wd, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(myPlanets.findElement(By.id("planetList")), By.tagName(DIV_TAG)));

        for(WebElement colonyWebElement : coloniesWebElements) {
            ColonyEntity colonyEntity = new ColonyEntity();

            colonyEntity.cp = Integer.parseInt(colonyWebElement.getAttribute(ID_ATTRIBUTE).split("-")[1]);
            WebElement element = colonyWebElement.findElement(By.className("planet-koords"));
            Planet planet = new Planet(element.getText());
            colonyEntity.galaxy = planet.galaxy;
            colonyEntity.system = planet.system;
            colonyEntity.position = planet.position;
            colonyEntity.type = ColonyType.PLANET;

            List<WebElement> moons = colonyWebElement.findElements(By.className("moonlink"));
            if(!moons.isEmpty()) {
                String link = moons.get(0).getAttribute(HREF_ATTRIBUTE);
                int i = link.indexOf("cp=");
                colonyEntity.cpm = Integer.parseInt(link.substring(i+3));

                ColonyEntity moonColonyEntity = new ColonyEntity();
                moonColonyEntity.galaxy = planet.galaxy;
                moonColonyEntity.system = planet.system;
                moonColonyEntity.position = planet.position;
                moonColonyEntity.type = ColonyType.MOON;
                moonColonyEntity.cp = colonyEntity.cpm;
                moonColonyEntity.cpm = colonyEntity.cp;
                colonyEntities.add(moonColonyEntity);
            }
            colonyEntities.add(colonyEntity);
        }

        String countColoniesText = myPlanets.findElement(By.id("countColonies")).getText();
        int expectedColoniesCount = Integer.parseInt(countColoniesText.substring(0, countColoniesText.indexOf("/")).trim());

        long actualColoniesCount = colonyEntities.stream().filter(colonyEntity -> colonyEntity.is(ColonyType.PLANET)).count();

        if(expectedColoniesCount != actualColoniesCount) {
            throw new RuntimeException("Incorrect colonies load - expected "+expectedColoniesCount+" but was "+coloniesWebElements.size());
        }
        return colonyEntities;
    }

}
