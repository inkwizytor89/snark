package org.enoch.snark.instance.si.module.fleet;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.FleetSendCommand;
import org.enoch.snark.action.command.RecallCommand;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.action.command.SendFleetPromiseCommand;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.service.PlanetService;
import org.enoch.snark.instance.si.Core;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.enoch.snark.instance.si.QueueRunType;
import org.enoch.snark.instance.model.action.FleetBuilder;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.action.filter.AbstractFilter;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.enoch.snark.action.command.FollowingAction.DELAY_TO_FLEET_BACK;
import static org.enoch.snark.action.command.FollowingAction.DELAY_TO_FLEET_THERE;
import static org.enoch.snark.instance.model.action.PlanetExpression.PLANET;
import static org.enoch.snark.instance.model.to.Resources.nothing;
import static org.enoch.snark.instance.model.to.ShipsMap.*;
import static org.enoch.snark.instance.si.module.ThreadMap.*;

@RequiredArgsConstructor
public class FleetThread extends AbstractThread {

    public static final String threadName = "fleet";

    private final Core core;
    private final PlanetService planetService;

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
        List<FleetPromise> fleetPromises = new FleetBuilder()
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
                .buildAll();

//        SendFleetPromiseCommand command = new SendFleetPromiseCommand(promise);



// bardzo duzo tergetów niech wygeneruje flot i moze wrzucajmy jakimiś partiami
        // kolejny iteracja by wrzuciła nastepna porcje ktora nie poleciala
        int index = 0;
        List<FleetSendCommand> fleetSendCommands = planetService.from(fleetPromises);
        for(FleetSendCommand command : fleetSendCommands) {

//        fleetPromises.forEach(promise -> {
            index++;
            logFleetOverview(command);
            boolean areShips = areShips(command.getPromise());
            boolean fit = command.getPromise().fit();
            if (fit && areShips && noBlockExpiredTime(command.getPromise())) {
                if (!map.getConfigBoolean(DRY_RUN, false)) {
                    command.setRunType(QueueRunType.valueOf(map.getConfig(QUEUE, QueueRunType.NORMAL.name())));
                    command.generateHash(map.name(), Integer.toString(index));

                    Duration recallDuration = map.getDuration(RECALL, null);
                    if(recallDuration != null) command.setNext(new RecallCommand(command.getPromise()), recallDuration.getSeconds());
                    core.push(command);
                }
            }
//        });
        }
    }

    private boolean areShips(FleetPromise promise) {
        boolean noShips = promise.normalizeShipMap().isEmpty();
        return !noShips;
    }

    private boolean noBlockExpiredTime(FleetPromise promise) {
        String expiredConfig = map.getConfig(EXPIRED_TIME, null);
        if(expiredConfig == null) return true;
        else throw new RuntimeException("Not implementet field "+EXPIRED_TIME);
        // todo: ponizej sa przypadki kiedy strzeba zajrzec do bazy by sprawdzic czy poprzednia flota osiagnela cel
        // albo wrocila albo minol czas od ostatniej i mozna juz wyslac nastepna
        // jeszcze jest problem ze recall wchodzi w konflit poprzez setNext
//        else if (DELAY_TO_FLEET_THERE.equals(expiredConfig)) command.push(DELAY_TO_FLEET_THERE);
//        else if (DELAY_TO_FLEET_BACK.equals(expiredConfig)) command.push(DELAY_TO_FLEET_BACK);
//        else command.push(LocalDateTime.now().minusSeconds(new Duration(expiredConfig).getSeconds()));
    }

    private void logFleetOverview(FleetSendCommand promise) {
        List<AbstractCondition> wontFit = promise.getPromise().wontFit();
        StringBuilder errorMessage = new StringBuilder(promise.toString());
        errorMessage.append(" wontFit: ");
        wontFit.forEach(condition -> errorMessage.append(condition.reason(promise.getPromise())).append(" "));
        log(errorMessage.toString());
    }
}
