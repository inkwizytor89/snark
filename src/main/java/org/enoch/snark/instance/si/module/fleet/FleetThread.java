package org.enoch.snark.instance.si.module.fleet;

import org.enoch.snark.common.time.Duration;
import org.enoch.snark.gi.command.impl.SendFleetPromiseCommand;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.commander.QueueRunType;
import org.enoch.snark.instance.model.action.FleetBuilder;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.action.filter.AbstractFilter;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ConfigMap;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.enoch.snark.gi.command.impl.FollowingAction.DELAY_TO_FLEET_BACK;
import static org.enoch.snark.gi.command.impl.FollowingAction.DELAY_TO_FLEET_THERE;
import static org.enoch.snark.instance.model.action.PlanetExpression.PLANET;
import static org.enoch.snark.instance.model.to.Resources.nothing;
import static org.enoch.snark.instance.model.to.ShipsMap.*;
import static org.enoch.snark.instance.si.module.ConfigMap.*;

public class FleetThread extends AbstractThread {

    public static final String threadName = "fleet";

    public FleetThread(ConfigMap map) {
        super(map);
    }

    @Override
    protected String getThreadType() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return 1;
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStep() {
        List<Entry<String, String>> conditionsEntry = map.entrySet().stream().filter(entry -> entry.getKey().startsWith("condition_")).toList();
        List<Entry<String, String>> filtersEntry = map.entrySet().stream().filter(entry -> entry.getKey().startsWith("filter_")).toList();
        List<SendFleetPromiseCommand> sendFleetCommands = new FleetBuilder()
                .from(map.getNearestConfig(SOURCE, PLANET))
                .to(map.getConfig(TARGET, null))
                .conditions(AbstractCondition.create(conditionsEntry))
                .filters(AbstractFilter.create(filtersEntry))
                .mission(Mission.convert(map.getConfig(MISSION, null)))
                .ships(map.getShipsWaves(singletonList(ALL_SHIPS)))
                .leaveShips(map.getShipsWaves(LEAVE_SHIPS_WAVE, EMPTY_SHIP_WAVE))
                .resources(map.getConfigResources(RESOURCES, nothing))
                .leaveResources(map.getConfigResources(LEAVE_RESOURCES, null))
                .speed(map.getConfigLong(SPEED, null))
                .recall(map.getDuration(RECALL, null))
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
                    push(command);
            }
        });
    }

    private boolean areShips(SendFleetPromiseCommand command) {
        boolean noShips = command.promise().normalizeShipMap().isEmpty();
        return !noShips;
    }

    private void push(SendFleetPromiseCommand command) {
        String expiredConfig = map.getConfig(EXPIRED_TIME, null);
        if(expiredConfig == null) command.push();
        else if (DELAY_TO_FLEET_THERE.equals(expiredConfig)) command.push(DELAY_TO_FLEET_THERE);
        else if (DELAY_TO_FLEET_BACK.equals(expiredConfig)) command.push(DELAY_TO_FLEET_BACK);
        else command.push(LocalDateTime.now().minusSeconds(new Duration(expiredConfig).getSeconds()));
    }

    private void logFleetOverview(SendFleetPromiseCommand command) {
        List<AbstractCondition> wontFit = command.promise().wontFit();
        StringBuilder errorMessage = new StringBuilder(command.hash());
        errorMessage.append(" wontFit: ");
        wontFit.forEach(condition -> errorMessage.append(condition.reason(command.promise())).append(" "));
        log(errorMessage.toString());
    }
}
