package org.enoch.snark.gi;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.model.Planet;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.enoch.snark.gi.macro.GIUrlBuilder.*;

public class BaseGameInfoGIR extends GraphicalInterfaceReader {

    public List<ColonyEntity> loadPlanetList() {
        ArrayList<ColonyEntity> colonyEntities = new ArrayList<>();
        List<WebElement> coloniesWebElements = new WebDriverWait(wd, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(wd.findElement(By.id("planetList")), By.tagName(DIV_TAG)));
        for(WebElement colonyWebElement : coloniesWebElements) {
            try {
                ColonyEntity colonyEntity = new ColonyEntity();

                colonyEntity.cp = Integer.parseInt(colonyWebElement.getAttribute(ID_ATTRIBUTE).split("-")[1]);
                WebElement element = colonyWebElement.findElement(By.className("planet-koords"));
                Planet planet = new Planet(element.getText());
                colonyEntity.galaxy = planet.galaxy;
                colonyEntity.system = planet.system;
                colonyEntity.position = planet.position;
                colonyEntity.isPlanet = true;

                List<WebElement> moons = colonyWebElement.findElements(By.className("moonlink"));
                if(!moons.isEmpty()) {
                    String link = moons.get(0).getAttribute(HREF_ATTRIBUTE);
                    int i = link.indexOf("cp=");
                    colonyEntity.cpm = Integer.parseInt(link.substring(i+3));

                    ColonyEntity moonColonyEntity = new ColonyEntity();
                    moonColonyEntity.galaxy = planet.galaxy;
                    moonColonyEntity.system = planet.system;
                    moonColonyEntity.position = planet.position;
                    moonColonyEntity.isPlanet = false;
                    moonColonyEntity.cp = colonyEntity.cpm;
                    moonColonyEntity.cpm = colonyEntity.cp;
                    colonyEntities.add(moonColonyEntity);
                }

                colonyEntities.add(colonyEntity);
//                System.err.println(colonyEntity.getCordinate()+" "+colonyEntity.cp+" "+colonyEntity.cpm);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return colonyEntities;
    }

    public void updateColony(ColonyEntity colony) {
        new GIUrlBuilder().openComponent(PAGE_RESOURCES, colony);
        new GIUrlBuilder().openComponent(PAGE_FACILITIES, colony);
        if(isLifeformAvailable() && colony.isPlanet) {
            new GIUrlBuilder().openComponent(PAGE_LIFEFORM, colony);
        }
        new GIUrlBuilder().openComponent(PAGE_BASE_FLEET, colony);
        new GIUrlBuilder().openComponent(PAGE_DEFENSES, colony);
    }

    public boolean isLifeformAvailable() {
        return !wd.findElements(By.id("lifeform")).isEmpty();
    }


}
