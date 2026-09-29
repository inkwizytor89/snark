package org.enoch.snark.instance.si.module.consumer.gi;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.SystemView;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.service.MessageService;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.enoch.snark.common.SleepUtil.pause;
import static org.enoch.snark.instance.model.types.ColonyType.MOON;
import static org.enoch.snark.instance.model.types.ColonyType.PLANET;


public class GalaxyGIR extends GraphicalInterfaceReader {

    public GalaxyGIR(Wd wd) {
        super(wd);
    }

    public void updateGalaxy(SystemView systemView, List<TargetEntity> targets) {
        for(WebElement row : wd().findElements(By.className("galaxyRow"))) {
            Optional<WebElement> positionOptional = row.findElements(By.className("cellPosition")).stream().findAny();
            if(positionOptional.isEmpty()) continue; // skip table headers
            final int position = Integer.parseInt(positionOptional.get().getText());

            if(false) {// after catching debris resources are always empty ""
                // debris
                List<WebElement> elements = wd().findElements(By.id("galaxyRow" + position));
                if (!elements.isEmpty()) {
                    List<WebElement> webElements = elements.get(0).findElements(By.cssSelector(".debris-content"));
                    if (!webElements.isEmpty()) {
                        for (WebElement web : webElements) {

                            WebElement microdebris = elements.get(0).findElement(By.className("microdebris"));
                            Actions actions = new Actions(wd());
                            actions.moveToElement(microdebris).perform();
                            SleepUtil.sleep();
                            String text = web.getText();
                            System.err.println(text);
                        }
                    }
                }


                Resources debrisResources = debrisToResource(row.findElements(By.className("debris-content")));
                Planet debris = new Planet(systemView.getGalaxy(), systemView.getSystem(), position, ColonyType.DEBRIS);
                Optional<TargetEntity> debrisTargetEntity = sync(targets, debris, !Resources.nothing.equals(debrisResources));
                debrisTargetEntity.ifPresent(targetEntity -> targetEntity.putResources(debrisResources));
            }
            // checking if row not empty
            List<WebElement> playerList = row.findElements(By.className("playerName"));
            if(playerList.isEmpty()) continue; //no player or ownPlayerRow

            //planet
            Planet planet = new Planet(systemView.getGalaxy(), systemView.getSystem(), position, PLANET);
            Optional<TargetEntity> planetTargetEntity = sync(targets, planet, true);

            //moon
            boolean isMoon = !row.findElements(By.className("micromoon")).isEmpty();
            Planet moon = new Planet(systemView.getGalaxy(), systemView.getSystem(), position, ColonyType.MOON);
            Optional<TargetEntity> moonTargetEntity = sync(targets, moon, isMoon);

            //player
            final WebElement playerElement = playerList.getFirst();
            final String playerCode = playerElement.getAttribute("rel").substring(6);

            if(planetTargetEntity.get().player == null || !planetTargetEntity.get().player.code.equals(playerCode)) {
                planetTargetEntity.get().player = new PlayerEntity();
            }
            PlayerEntity playerEntity = planetTargetEntity.get().player;

            playerEntity.code = playerCode;
            playerEntity.name = playerElement.getText().trim();
            playerEntity.alliance = row.findElement(By.className("cellAlliance")).getText();
            List<WebElement> statusList = row.findElement(By.className("cellPlayerName")).findElements(By.tagName("pre"));
            String status = statusList.isEmpty() ? "" : statusList.getFirst().getText();
            playerEntity.status = status;
            playerEntity.type = setStatus(status);

            moonTargetEntity.ifPresent(targetEntity -> targetEntity.player = playerEntity);
        }
    }

    private Resources debrisToResource(List<WebElement> elements) {
        if(elements.isEmpty()) return Resources.nothing;
        String resourcesString = StringUtils.EMPTY;
        for (int i = 0; i<elements.size(); i++) {
            String[] parts = elements.get(i).getText().split(" ");
            String last = parts[parts.length - 1];
            if(i == 0) resourcesString+= "m"+last;
            else if(i == 1) resourcesString+= "c"+last;
            else if(i == 2) resourcesString+= "d"+last;
        }
        return new Resources(resourcesString);
    }

    private Optional<TargetEntity> findTarget(List<TargetEntity> targets, Integer position, ColonyType type) {
        return targets.stream()
                .filter(t -> t.position.equals(position))
                .filter(t -> type.equals(t.type))
                .findAny();
    }

    private Optional<TargetEntity> sync(List<TargetEntity> targets, Planet coordinate, boolean exist) {
        Optional<TargetEntity> optionalTarget = findTarget(targets, coordinate.position, coordinate.type);
        if(optionalTarget.isPresent() && exist) {
            return optionalTarget;
        } else if(optionalTarget.isPresent()) {
            optionalTarget.get().updated = null;
            return optionalTarget;
        } else if(exist) {
            TargetEntity entity = new TargetEntity();
            entity.galaxy = coordinate.galaxy;
            entity.system = coordinate.system;
            entity.position = coordinate.position;
            entity.type = coordinate.type;
            entity.tags = coordinate.toString();
            targets.add(entity);
            return Optional.of(entity);
        }
        return Optional.empty();
    }

    public static String setStatus(String status) {
        if (status.contains("A")) {
            return TargetEntity.ADMIN;
        } else if (status.contains("u")) {
            return TargetEntity.ABSENCE;
        } else if (status.contains("i") || status.contains("I")) {
            return TargetEntity.IN_ACTIVE;
        } else if (status.contains("s")) {
            return TargetEntity.WEAK;
        } else {
            return TargetEntity.NORMAL;
        }
    }

    public void takeAction(Map<Planet, Boolean> positions, Mission mission) {
        for(WebElement row : wd().findElements(By.className("galaxyRow"))) {
            Optional<WebElement> positionOptional = row.findElements(By.className("cellPosition")).stream().findAny();
            if(positionOptional.isEmpty()) continue; // skip table headers
            final int position = Integer.parseInt(positionOptional.get().getText());

            Optional<Map.Entry<Planet, Boolean>> positionEntry = positions.entrySet().stream()
                    .filter(entry -> entry.getKey().position.equals(position)).findAny();
            boolean toTake = positionEntry.isPresent() && !positionEntry.get().getValue();
            if (Mission.SPY.equals(mission) && toTake) {
                spy(row, positionEntry.get());
            }
        }
    }

    private void spy(WebElement row, Map.Entry<Planet, Boolean> entry) {
        Planet coordinate = entry.getKey();
        if(PLANET.equals(coordinate.type)) {
            spyClick(entry, row, "microplanet");
        } else if(MOON.equals(coordinate.type)) {
            spyClick(entry, row, "micromoon");
        }
    }

    private void spyClick(Map.Entry<Planet, Boolean> entry, WebElement row, String tag) {
        List<WebElement> list = row.findElements(By.className(tag));
        if(!list.isEmpty()) {
            list.getFirst().click();
            boolean fleetHostile = false;
            for(int i = 0; i<4; i++) {
                pause();
                fleetHostile = !list.getFirst().findElements(By.className("fleetHostile")).isEmpty();
                if(fleetHostile) break;
            }
            if(fleetHostile) MessageService.getInstance().put(entry.getKey());
            entry.setValue(fleetHostile);
        }
    }
}
