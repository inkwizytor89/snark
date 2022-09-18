package org.enoch.snark.gi.command.impl;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.command.GICommand;
import org.enoch.snark.gi.macro.FleetSelector;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.SystemView;
import org.enoch.snark.model.exception.FleetCantStart;
import org.enoch.snark.model.exception.ToStrongPlayerException;
import org.enoch.snark.model.service.MessageService;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

import static org.enoch.snark.gi.command.CommandType.FLEET_REQUIERED;

public class SendFleetCommand extends GICommand {

    protected final FleetSelector fleetSelector;
//    protected TargetEntity target;
    public Mission mission;
//    protected SourcePlanet source;
protected GIUrlBuilder giUrlBuilder;

    public FleetEntity fleet;

    public SendFleetCommand(FleetEntity fleet) {
        super(FLEET_REQUIERED);
        this.fleet = fleet;
        this.mission = Mission.convertFromString(fleet.type);

        giUrlBuilder = new GIUrlBuilder();
        fleetSelector = new FleetSelector(instance.session);
    }

    @Override
    public boolean execute() {
//        fleet = FleetDAO.getInstance().fetch(fleet);
        if(fleet.visited != null || fleet.back != null) {
            System.err.println("Fleet already send "+fleet);
            return true;
        }
        // musimy pobrac odpowiednia flote z bazy danych
        // uzupełnić odpowiednimi danymi
        // w przypadku odpowiednich misji odpowiednie after comandy powinny zostać zaktualizowane
        giUrlBuilder.openFleetView(fleet.source, new Planet(fleet.getCoordinate()), mission);
        //Scroll down till the bottom of the page
        ((JavascriptExecutor) webDriver).executeScript("window.scrollBy(0,document.body.scrollHeight)");

        for(Map.Entry<ShipEnum, Long> entry : buildShipsMap().entrySet()) {
            fleetSelector.typeShip(entry.getKey(), entry.getValue());
        }

        fleetSelector.next();
//        SleepUtil.pause();
//        WebElement coordsElement = webDriver.findElement(By.id("target")).findElement(By.className("coords"));
//        coordsElement.findElement(By.id("galaxy")).sendKeys(fleet.targetGalaxy.toString());
//        coordsElement.findElement(By.id("system")).sendKeys(fleet.targetSystem.toString());
//        coordsElement.findElement(By.id("position")).sendKeys(fleet.targetPosition.toString());
//        SleepUtil.pause();
//        webDriver.findElement(By.id("target")).findElement(By.id("pbutton")).click();
        SleepUtil.pause();

        final String duration = instance.gi.findElement("span", "id", "duration", "").getText();
        //Text '' could not be parsed at index 0 - popular error, shoud wait for not null time
        final LocalTime durationTime = DateUtil.parseDuration(duration);
         String arrivalTimeString = webDriver.findElement(By.id("arrivalTime")).getText();
        if(arrivalTimeString.contains("-")) {
            SleepUtil.sleep();
            arrivalTimeString = webDriver.findElement(By.id("arrivalTime")).getText();
        }
        fleet.visited = DateUtil.parseToLocalDateTime(arrivalTimeString);
        final String returnTimeString = webDriver.findElement(By.id("returnTime")).getText();
        fleet.back = DateUtil.parseToLocalDateTime(returnTimeString);
//        setSecoundToDelayAfterCommand(durationTime.toSecondOfDay()+ 5L);
//        fleetSelector.next();
        if(webDriver.findElements(By.className("status_abbr_noob")).size() != 0) {//player is green - too weak
            Optional<TargetEntity> target = TargetDAO.getInstance().find(fleet.targetGalaxy, fleet.targetSystem, fleet.targetPosition);
            if (target.isPresent()) {
                PlayerEntity player = target.get().player;
                player.type = TargetEntity.WEAK;
                PlayerDAO.getInstance().saveOrUpdate(player);
            } else {
                // look at galaxy to reload player
                giUrlBuilder.openGalaxy(new SystemView(fleet.targetGalaxy, fleet.targetSystem), null);
            }
            return true;
        }
        if(Mission.SPY.equals(mission)) {
            MessageService.getInstance().put(durationTime.toSecondOfDay());
//            setAfterCommand(new ReadMessageCommand(instance));
        }

        try {
            fleetSelector.start();
        } catch(FleetCantStart e) {
            e.printStackTrace();
            Planet target = new Planet(fleet.targetGalaxy, fleet.targetSystem, fleet.targetPosition);
            System.err.println("Can not send fleer to target " + target);
            instance.push(new GalaxyAnalyzeCommand(new SystemView(fleet.targetGalaxy, fleet.targetSystem)));
//            instance.removePlanet(new Planet(fleet.getCoordinate()));
            setAfterCommand(null);
            return true;
        }
        catch(ToStrongPlayerException e) {
            System.err.println(e);
            setAfterCommand(null);
        }
        FleetDAO.getInstance().saveOrUpdate(fleet);
        new GIUrlBuilder().loadFleetStatus();
        return true;
    }

    public Map<ShipEnum, Long> buildShipsMap() {
        return ShipEnum.createShipsMap(fleet);
    }

    @Override
    public void onInterrupt() {
        super.onInterrupt();
        fleet.code = -1L;
        FleetDAO.getInstance().saveOrUpdate(fleet);
    }

    @Override
    public String toString() {
        return mission.name()+" "+fleet.getCoordinate()+" form "+fleet.source;
    }
}
