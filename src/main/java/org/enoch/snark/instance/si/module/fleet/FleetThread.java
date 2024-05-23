package org.enoch.snark.instance.si.module.fleet;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.commander.QueueRunType;
import org.enoch.snark.instance.model.action.FleetBuilder;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.action.condition.ConditionFactory;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ConfigMap;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.enoch.snark.instance.model.action.PlanetExpression.PLANET;
import static org.enoch.snark.instance.model.action.condition.ConditionType.*;
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
        List<Entry<String, String>> conditionsEntry = map.entrySet().stream().filter(entry -> entry.getKey().startsWith("condition_")).toList();
        List<SendFleetCommand> sendFleetCommands = new FleetBuilder()
                .from(map.getNearestConfig(SOURCE, PLANET))
                .to(map.getConfig(TARGET, null))

//                .condition(ConditionFactory.create(RESOURCE, map.getConfigResources(CONDITION_RESOURCES, null)))
//                .condition(ConditionFactory.create(RESOURCES_COUNT, map.getConfigNumber(CONDITION_RESOURCES_COUNT, null)))
//                .condition(ConditionFactory.create(SHIPS, map.getShips(CONDITION_SHIPS, null)))
//                .condition(ConditionFactory.create(NO_MISSIONS, Mission.parse(map.getConfig(CONDITION_BLOCKING_MISSIONS, null))))
                .condition(AbstractCondition.create(conditionsEntry))

                .mission(Mission.convert(map.getConfig(MISSION, null)))
                .ships(map.getShipsWaves(singletonList(ALL_SHIPS)))
                .leaveShips(map.getShipsWaves(LEAVE_SHIPS_WAVE, EMPTY_SHIP_WAVE))
                .resources(map.getConfigResources(RESOURCES, nothing))
                .leaveResources(map.getConfigResources(LEAVE_RESOURCES, nothing))
                .speed(map.getConfigLong(SPEED, null))
                .queue(QueueRunType.valueOf(map.getConfig(QUEUE, QueueRunType.NORMAL.name())))
                .hashPrefix(map.name())
                .buildAll();
// bardzo duzo tergetów niech wygeneruje flot i moze wrzucajmy jakimiś partiami
        // kolejny iteracja by wrzuciła nastepna porcje ktora nie poleciala
        sendFleetCommands.forEach(command -> {
            logFleetOverview(command);
            boolean areShips = areShips(command);
            boolean fit = command.promise().fit();
            if(fit && areShips) {
                if (!map.getConfigBoolean(DRY_RUN, false))
                    command.push(dateToCheck());
            }
        });
    }

    private boolean areShips(SendFleetCommand command) {
        ColonyEntity source = command.fleet.source;
        boolean noShips = command.promise().calculateShipMap(source).isEmpty();
        return !noShips;
    }

    private LocalDateTime dateToCheck() {
        LocalTime time = map.getLocalTime(EXPIRED_TIME, null);
        if(time == null) return LocalDateTime.now();
        return LocalDateTime.now().minusHours(time.getHour()).minusMinutes(time.getMinute());
    }

    private void logFleetOverview(SendFleetCommand command) {
        List<AbstractCondition> wontFit = command.promise().wontFit();
        StringBuilder errorMessage = new StringBuilder(command.hash());
        errorMessage.append(" wontFit: ");
        wontFit.forEach(condition -> errorMessage.append(condition.reason(command.promise().getSource())).append(" "));
        log(errorMessage.toString());
    }
}
