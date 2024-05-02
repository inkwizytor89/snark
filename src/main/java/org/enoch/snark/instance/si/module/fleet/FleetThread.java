package org.enoch.snark.instance.si.module.fleet;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.model.action.FleetBuilder;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ConfigMap;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.enoch.snark.instance.model.to.Resources.nothing;
import static org.enoch.snark.instance.model.to.ShipsMap.*;
import static org.enoch.snark.instance.si.module.ConfigMap.*;

public class FleetThread extends AbstractThread {

    public static final String threadName = "fleet";

    public FleetThread(ConfigMap map) {
        super(map);
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return pause;
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStep() {
        List<SendFleetCommand> sendFleetCommands = new FleetBuilder()
                .from(map.getNearestConfig(SOURCE, PLANET))
                .to(map.getConfig(TARGET, null))
                .conditionShip(map.getShipsWaves(CONDITION_SHIPS_WAVE, singletonList(NO_SHIPS)).get(0))
                .conditionResourceCount(map.getConfigLong(CONDITION_RESOURCES_COUNT, null))
                .conditionResource(map.getConfigResource(CONDITION_RESOURCES, null))
                .ships(map.getShipsWaves(singletonList(ALL_SHIPS)))
                .leaveShips(map.getShipsWaves(LEAVE_SHIPS_WAVE, EMPTY_SHIP_WAVE))
                .mission(Mission.convert(map.getConfig(MISSION, null)))
                .resources(map.getConfigResource(RESOURCES, nothing))
                .speed(map.getConfigLong(SPEED, null))
                .buildAll();

        sendFleetCommands.stream()
                .filter(this::noSkip)
                .forEach(fleetCommand -> fleetCommand.push(dateToCheck()));
    }

    private boolean noSkip(SendFleetCommand command) {
        ColonyEntity source = command.fleet.source;
        return !command.promise().calculateShipMap(source).isEmpty();
    }

    private LocalDateTime dateToCheck() {
        LocalTime time = map.getLocalTime(EXPIRED_TIME, null);
        if(time == null) return LocalDateTime.now();
        return LocalDateTime.now().minusHours(time.getHour()).minusMinutes(time.getMinute());
    }
}
