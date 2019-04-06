package org.enoch.snark.gi.command.impl;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.command.GICommand;
import org.enoch.snark.gi.macro.FleetSelector;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.SpyInfo;
import org.enoch.snark.model.exception.PlanetDoNotExistException;
import org.enoch.snark.model.exception.ToStrongPlayerException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.enoch.snark.gi.command.CommandType.FLEET_REQUIERED;

public class SendFleetCommand extends GICommand {

    private final FleetSelector fleetSelector;
//    protected TargetEntity target;
    public Mission mission;
//    protected SourcePlanet source;
    private GIUrlBuilder giUrlBuilder;

    public FleetEntity fleet;

    public SendFleetCommand(Instance instance, FleetEntity fleet) {
        super(instance, FLEET_REQUIERED);
        this.fleet = fleet;
        this.mission = Mission.convertFromString(fleet.type);

        giUrlBuilder = new GIUrlBuilder(instance);
        fleetSelector = new FleetSelector(instance.session);
    }

    @Override
    public boolean execute() {
        fleet = instance.daoFactory.fleetDAO.fetch(fleet);
        if(fleet.visited != null || fleet.back != null) {
            System.err.println("Fleet already send "+fleet);
            return true;
        }
        // musimy pobrac odpowiednia flote z bazy danych
        // uzupełnić odpowiednimi danymi
        // w przypadku odpowiednich misji odpowiednie after comandy powinny zostać zaktualizowane
        giUrlBuilder.openFleetView(fleet.source, fleet.target, mission);

        for(Map.Entry<ShipEnum, Long> entry : ShipEnum.createShipsMap(fleet).entrySet()) {
            fleetSelector.typeShip(entry.getKey(), entry.getValue());
        }
        fleetSelector.next();

        instance.session.sleep(TimeUnit.SECONDS, 1);
        final String duration = webDriver.findElement(By.id("duration")).getText();
        final LocalTime durationTime = DateUtil.parse(duration);
         String arrivalTimeString = webDriver.findElement(By.id("arrivalTime")).getText();
        if(arrivalTimeString.contains("-")) {
            instance.session.sleep(TimeUnit.SECONDS, 2);
            arrivalTimeString = webDriver.findElement(By.id("arrivalTime")).getText();
        }
        fleet.visited = DateUtil.parseToLocalDateTime(arrivalTimeString);
        final String returnTimeString = webDriver.findElement(By.id("returnTime")).getText();
        fleet.back = DateUtil.parseToLocalDateTime(returnTimeString);
        setSecoundToDelayAfterCommand(durationTime.toSecondOfDay()+ 5);
        fleetSelector.next();
        if(webDriver.findElements(By.className("status_abbr_noob")).size() != 0) {//player is green - too weak
            TargetEntity target = fleet.target;
            target.type = TargetEntity.WEAK;
            instance.daoFactory.targetDAO.saveOrUpdate(target);
            return true;
        }
        if(Mission.SPY.equals(mission)) {
            setAfterCommand(new ReadMessageCommand(instance));
        }

        try {
            fleetSelector.start();
        } catch(PlanetDoNotExistException e) {
            e.printStackTrace();
            instance.removePlanet(fleet.target);
            setAfterCommand(null);
            // TODO: thred don't get information about one fleet slot free
            return true;
        }
        catch(ToStrongPlayerException e) {
            System.err.println(e);
            setAfterCommand(null);
        }
        instance.daoFactory.fleetDAO.saveOrUpdate(fleet);
        return true;
    }

    @Override
    public String toString() {
        return mission.name()+" "+fleet.target+" form "+fleet.source;
    }
}
