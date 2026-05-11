package org.enoch.snark.instance.si.module.fleet;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.snark.action.command.FleetSendCommand;
import org.enoch.snark.action.command.RecallCommand;
import org.enoch.snark.common.DateUtil;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.db.repository.FleetRepository;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.action.condition.ShipsCondition;
import org.enoch.snark.instance.model.to.*;
import org.enoch.snark.instance.service.ConditionChecker;
import org.enoch.snark.instance.service.FleetDispatcher;
import org.enoch.snark.instance.service.CoordinateExpressionService;
import org.enoch.snark.instance.service.ShipService;
import org.enoch.snark.instance.si.Core;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.enoch.snark.instance.si.QueueRunType;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.enoch.snark.instance.model.to.Resources.nothing;
import static org.enoch.snark.instance.model.to.ShipsMap.*;
import static org.enoch.snark.instance.service.CoordinateExpressionService.PLANETS;
import static org.enoch.snark.instance.si.module.ThreadMap.*;

@RequiredArgsConstructor
public class FleetThread extends AbstractThread {

    public static final String threadName = "fleet";

    private final Core core;
    private final FleetRepository fleetRepository;
    private final FleetDispatcher fleetDispatcher;
    private final ConditionChecker conditionChecker;
    private final CoordinateExpressionService coordinateExpressionService;
    private final ShipService shipService;

    private int size = 0;
    private HashMap<String, LocalDateTime> blockingMap = new HashMap<>();
    private String expiredConfig;

    @Override
    protected String getThreadType() {
        return threadName;
    }

    @Override
    protected String defaultPause() {
        return "10S";
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStep() {
        if(container.anyNotProcessed()) return; // no need to find next if waiting is more than limit
//        List<Entry<String, String>> conditionsEntry = map.entrySet().stream().filter(entry -> entry.getKey().startsWith("condition_")).toList();
        List<Entry<String, String>> filtersEntry = map.entrySet().stream().filter(entry -> entry.getKey().startsWith("filter_")).toList();
        FleetPlan fleetPlan = FleetPlan.builder()
                .source(getNearestConfig(SOURCE, PLANETS))
                .target(map.getConfig(TARGET, null))
                .conditions(getConditions(START_CONDITION))
//                .filters(AbstractFilter.create(filtersEntry))
                .mission(Mission.convert(map.getConfig(MISSION, Mission.STATIONED.name())))
                .shipsWaves(map.getShipsWaves(singletonList(ALL_SHIPS)))
                .leaveShips(map.getShipsWaves(LEAVE_SHIPS_WAVE, EMPTY_SHIP_WAVE))
                .resources(map.getConfigResources(RESOURCES, nothing))
                .leaveResources(map.getConfigResources(LEAVE_RESOURCES, null))
                .speed(map.getConfigLong(SPEED, null))
                .trip(coordinateSpelService.nonCached(map.getConfig(TRIP, null)))
                .build();

        List<FleetSendCommand> fleetSendCommands = fleetDispatcher.from(fleetPlan);

        Map<String, Boolean> containerToRelease = createMapToRelease();
        expiredConfig = map.getConfig(EXPIRED_TIME, null);
        for(FleetSendCommand command : fleetSendCommands) {
            command.generateHash(map.name(), "X");
            containerToRelease.remove(command.getHash());
            if (isBlocked(command)) continue;
//            if(container.contains(command.getHash())) continue;
//            logFleetOverview(command);

            FleetContext fleetContext = FleetContext.builder()
                    .source(new PlanetData(command.getSource()))
                    .target(command.getTarget())
                    .leaveShipsMap(command.getLeaveShipsMap())
                    .build();

            ShipsMap realShips = shipService.fromExpressionToValues(command.getShipsMap(), fleetContext);
            if(realShips.isEmpty()) continue;

            List<AbstractCondition> conditionsToCheck = new ArrayList<>(command.getConditions());
            AbstractCondition check = conditionChecker.check(conditionsToCheck, command);
            if(check != null) {
                log(command.getHash()+" don't fit "+check.getClass().getSimpleName());
            }
            if(check != null) continue;
//conditionChecker.check(command.getConditions(), command).getClass().getSimpleName() +" "+command.getTarget().getTarget().getResources().count()
//            if (blockExpiredTime(command)) continue;

            if (!map.getConfigBoolean(DRY_RUN, false)) {
                command.setRunType(QueueRunType.valueOf(map.getConfig(QUEUE, QueueRunType.NORMAL.name())));
                command.addConditions(singletonList(new ShipsCondition(realShips, command.getLeaveShipsMap(), command.getSource().toPlanet())));

                Duration recallDuration = map.getDuration(RECALL, null);
                if(recallDuration != null) command.setNext(new RecallCommand(command), recallDuration.getSeconds());
                pushCommand(command);
            }
        }
        if(fleetSendCommands.size() != size) {

            size = fleetSendCommands.size();
            System.err.println("Fleets "+map().name()+" in map "+size);
            fleetSendCommands.forEach(fleetSendCommand -> System.err.print(fleetSendCommand+"="+fleetSendCommand.getStatus().getStatus()+", "));
        }
        containerToRelease.keySet().forEach(s -> container.removeKey(s));
    }

    private Map<String, Boolean> createMapToRelease() {
        return this.container.peek().stream().map(AbstractCommand::getHash)
                .collect(Collectors.toMap(s -> s, _ -> Boolean.TRUE));
    }

    private boolean isBlocked(FleetSendCommand command) {
        if(expiredConfig == null) return false;
        String hash = command.getHash();
        if(!blockingMap.containsKey(hash)) blockingMap.put(hash, fleetRepository.findExpiredTime(hash, expiredConfig).orElse(null));
        LocalDateTime expireDate = blockingMap.get(hash);
//        boolean isExpired = LocalDateTime.now().isBefore(expireDate);
        boolean isExpired = DateUtil.isExpired(expireDate);
        if(isExpired) {
            blockingMap.remove(hash);
        }
//        System.err.println("is isBlocked "+ !isExpired + " command "+hash);
        return !isExpired;
//        return fleetRepository.isBlockedWithExpiredTime(command.getHash(), expiredConfig);
    }

//    private void logFleetOverview(FleetSendCommand promise) {
//        List<AbstractPromiseCondition> wontFit = promise.getFleetPlan().wontFit();
//        StringBuilder errorMessage = new StringBuilder(promise.toString());
//        errorMessage.append(" wontFit: ");
//        wontFit.forEach(condition -> errorMessage.append(condition.reason(promise.getFleetPlan())).append(" "));
//        log(errorMessage.toString());
//    }
}
