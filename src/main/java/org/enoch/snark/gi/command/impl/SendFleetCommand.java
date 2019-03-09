package org.enoch.snark.gi.command.impl;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.entity.FleetEntity;
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

import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.enoch.snark.gi.command.CommandType.FLEET_REQUIERED;

public class SendFleetCommand extends GICommand {

    private final FleetSelector fleetSelector;
//    protected TargetEntity target;
    private Mission mission;
//    protected SourcePlanet source;
    private GIUrlBuilder giUrlBuilder;

    private final FleetEntity fleet;

    public SendFleetCommand(Instance instance, FleetEntity fleet) {
        super(instance, FLEET_REQUIERED);
        this.fleet = fleet;
        this.mission = Mission.convertFromString(fleet.type);

        giUrlBuilder = new GIUrlBuilder(instance);
        fleetSelector = new FleetSelector(instance.session);
    }

    @Override
    public boolean execute() {

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
        final LocalTime time = DateUtil.parse(duration);
        setSecoundToDelayAfterCommand(time.toSecondOfDay()+ 5);
        fleetSelector.next();

        if(Mission.SPY.equals(mission)) {
            setAfterCommand(ReadMessageCommand);
        }

        try {
            fleetSelector.start();
        } catch(PlanetDoNotExistException | ToStrongPlayerException e) {
            if(getAfterCommand() instanceof SpyReporter) {
                SpyReporter reporter = (SpyReporter) getAfterCommand();
                final SpyInfo nonExistingPlanetInfo = new SpyInfo(target);
                nonExistingPlanetInfo.source = instance.findNearestSource(target);
                reporter.getSpyObserver().report(nonExistingPlanetInfo);
            }
            setAfterCommand(null);
        }
        return true;
    }

    @Override
    public String toString() {
        return mission.name()+" "+fleet.target+" form "+fleet.source;
    }
}
