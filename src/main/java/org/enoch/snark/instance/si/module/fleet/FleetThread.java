package org.enoch.snark.instance.si.module.fleet;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.FleetSendCommand;
import org.enoch.snark.action.command.RecallCommand;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.repository.FleetRepository;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.to.FleetPlan;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.service.ConditionChecker;
import org.enoch.snark.instance.service.FleetDispatcher;
import org.enoch.snark.instance.service.PlanetService;
import org.enoch.snark.instance.si.Core;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.enoch.snark.instance.si.QueueRunType;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.enoch.snark.action.command.FollowingAction.DELAY_TO_FLEET_BACK;
import static org.enoch.snark.action.command.FollowingAction.DELAY_TO_FLEET_THERE;
import static org.enoch.snark.instance.model.action.PlanetExpression.PLANET;
import static org.enoch.snark.instance.model.to.Resources.nothing;
import static org.enoch.snark.instance.model.to.ShipsMap.*;
import static org.enoch.snark.instance.model.uc.ShipUC.fromExpressionToValues;
import static org.enoch.snark.instance.si.module.ThreadMap.*;

@RequiredArgsConstructor
public class FleetThread extends AbstractThread {

    public static final String threadName = "fleet";

    private final Core core;
    private final FleetRepository fleetRepository;
    private final FleetDispatcher fleetDispatcher;
    private final ConditionChecker conditionChecker;
    private final PlanetService planetService;

    @Override
    protected String getThreadType() {
        return threadName;
    }

    @Override
    protected String defaultPause() {
        return "1S";
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStep() {
        List<Entry<String, String>> conditionsEntry = map.entrySet().stream().filter(entry -> entry.getKey().startsWith("condition_")).toList();
        List<Entry<String, String>> filtersEntry = map.entrySet().stream().filter(entry -> entry.getKey().startsWith("filter_")).toList();
        FleetPlan fleetPlan = FleetPlan.builder()
                .source(map.getNearestConfig(SOURCE, PLANET))
                .target(map.getConfig(TARGET, null))
//                .conditions(AbstractCondition.create(conditionsEntry))
//                .filters(AbstractFilter.create(filtersEntry))
                .mission(Mission.convert(map.getConfig(MISSION, Mission.STATIONED.name())))
                .shipsWaves(map.getShipsWaves(singletonList(ALL_SHIPS)))
                .leaveShips(map.getShipsWaves(LEAVE_SHIPS_WAVE, EMPTY_SHIP_WAVE))
                .resources(map.getConfigResources(RESOURCES, nothing))
                .leaveResources(map.getConfigResources(LEAVE_RESOURCES, null))
                .speed(map.getConfigLong(SPEED, null))
                .trip(planetService.fromExpression(map.getConfig(TRIP, null)))
                .build();

// bardzo duzo tergetów niech wygeneruje flot i moze wrzucajmy jakimiś partiami
        // kolejny iteracja by wrzuciła nastepna porcje ktora nie poleciala
        int index = 0;
        List<FleetSendCommand> fleetSendCommands = fleetDispatcher.from(fleetPlan);
        for(FleetSendCommand command : fleetSendCommands) {

            index++;
            command.generateHash(map.name(), Integer.toString(index));
//            logFleetOverview(command);
            ShipsMap realShips = fromExpressionToValues(command.getShipsMap(), command.getSource(), command.getLeaveShipsMap());
            if(realShips.isEmpty()) continue;

            List<AbstractCondition> conditionsToCheck = new ArrayList<>(command.getConditions());
            if(!conditionChecker.fit(conditionsToCheck)) continue;

            if (blockExpiredTime(command)) continue;

            if (!map.getConfigBoolean(DRY_RUN, false)) {
                command.setRunType(QueueRunType.valueOf(map.getConfig(QUEUE, QueueRunType.NORMAL.name())));

                Duration recallDuration = map.getDuration(RECALL, null);
                if(recallDuration != null) command.setNext(new RecallCommand(command), recallDuration.getSeconds());
                log(command.toString());
                core.push(command);
            }
        }
    }

    private boolean blockExpiredTime(FleetSendCommand command) {
        String expiredConfig = map.getConfig(EXPIRED_TIME, null);
        if(expiredConfig == null) return false;
        Optional<FleetEntity> lastSend = fleetRepository.findFirstByHashOrderByUpdatedDesc(command.getHash());
        if(lastSend.isEmpty()) return false;

        else if (DELAY_TO_FLEET_THERE.equals(expiredConfig)) return LocalDateTime.now().isBefore(lastSend.get().visited);
        else if (DELAY_TO_FLEET_BACK.equals(expiredConfig)) return LocalDateTime.now().isBefore(lastSend.get().back);
        else return LocalDateTime.now().isBefore(lastSend.get().updated.plusSeconds(new Duration(expiredConfig).getSeconds()));
    }

//    private void logFleetOverview(FleetSendCommand promise) {
//        List<AbstractPromiseCondition> wontFit = promise.getFleetPlan().wontFit();
//        StringBuilder errorMessage = new StringBuilder(promise.toString());
//        errorMessage.append(" wontFit: ");
//        wontFit.forEach(condition -> errorMessage.append(condition.reason(promise.getFleetPlan())).append(" "));
//        log(errorMessage.toString());
//    }
}
