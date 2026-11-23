package org.enoch.snark.instance.si.module.fleet;

import lombok.RequiredArgsConstructor;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.snark.action.command.FleetSendCommand;
import org.enoch.snark.action.command.RecallCommand;
import org.enoch.snark.common.DateUtil;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.db.repository.FleetRepository;
import org.enoch.snark.db.repository.ConfigRepository;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.action.condition.AreShipsCondition;
import org.enoch.snark.instance.model.to.*;
import org.enoch.snark.instance.service.ConditionChecker;
import org.enoch.snark.instance.service.FleetDispatcher;
import org.enoch.snark.instance.service.ShipService;
import org.enoch.snark.instance.si.Core;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.enoch.snark.instance.model.to.ShipsMap.*;
import static org.enoch.snark.instance.si.module.ThreadMap.*;

@RequiredArgsConstructor
public class FleetThread extends AbstractThread {

    public static final String threadName = FleetDefinition.THREAD_TYPE;

    private final Core core;
    private final FleetRepository fleetRepository;
    private final FleetDispatcher fleetDispatcher;
    private final ConditionChecker conditionChecker;
    private final ShipService shipService;
    private final ConfigRepository configRepository;

    private int size = 0;
    private HashMap<String, LocalDateTime> blockingMap = new HashMap<>();
    private String expiredConfig;
    private FleetDefinition config;

    @Override
    protected void onStart() {
        super.onStart();
        config = new FleetDefinition();
        config.update(configRepository.findByThread_Module_NameAndThread_Name(
                threadEntity.module.name, "main"));
        config.update(threadEntity.configs);
    }

    @Override
    protected void onStep() {
        if(container.anyNotProcessed()) return; // no need to find next if waiting is more than limit
        FleetPlan fleetPlan = FleetPlan.builder()
                .source(config.getEffectiveSource())
                .target(config.getTargetValue())
                .conditions(expressionService.evaluateConditions(config.getStartConditionsValue(), Map.of()))
                .mission(config.getMissionValue())
                .shipsWaves(config.getShipsWavesValue())
                .leaveShips(config.getLeaveShipsWavesValue())
                .resources(config.getResourcesValue())
                .leaveResources(config.getLeaveResourcesValue())
                .speed(config.getSpeedValue())
                .trip(expressionService.evaluateCoordinates(config.getTripValue()))
                .build();

        List<FleetSendCommand> fleetSendCommands = fleetDispatcher.from(fleetPlan);

        Map<String, Boolean> containerToRelease = createMapToRelease();
        expiredConfig = config.getExpiredTimeValue();
        for(FleetSendCommand command : fleetSendCommands) {
            command.generateHash(threadEntity.name, "X");
            containerToRelease.remove(command.getHash());
            if (isBlocked(command)) continue;
//            if(container.contains(command.getHash())) continue;
//            logFleetOverview(command);

            if(command.getSource() == null )
                throw new RuntimeException("Fleet source is null where threat source was "+config.getEffectiveSource()+ " and generated "+fleetSendCommands.size() + " commands");

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

            if (!Boolean.TRUE.equals(config.getDryRun())) {
                command.setRunType(config.getQueueValue());
                command.addConditions(singletonList(new AreShipsCondition(realShips, command.getLeaveShipsMap(), command.getSource().toPlanet())));

                Duration recallDuration = config.getRecallValue();
                if(recallDuration != null) command.setNext(new RecallCommand(command), recallDuration.getSeconds());
                pushCommand(command);
            }
        }
        if(fleetSendCommands.size() != size) {

            size = fleetSendCommands.size();
            System.err.println("Fleets "+threadEntity.name+" in map "+size);
            fleetSendCommands.forEach(fleetSendCommand -> System.err.print(fleetSendCommand+"="+fleetSendCommand.getStatus().getStatus()+", "));
        }
        containerToRelease.keySet().forEach(s -> container.removeKey(s));
    }

    @Override
    public ThreadDefinition threadConfiguration() {
        return config.threadConfiguration();
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
