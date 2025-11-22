package org.enoch.snark.action.processor;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.SendCommand;
import org.enoch.snark.action.command.status.ExecutionIssue;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.FleetRepository;
import org.enoch.snark.db.repository.PlayerRepository;
import org.enoch.snark.db.repository.TargetRepository;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.action.condition.FleetSlotCondition;
import org.enoch.snark.instance.model.action.condition.ResourceCondition;
import org.enoch.snark.instance.model.action.condition.ShipsCondition;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.model.to.SystemView;
import org.enoch.snark.instance.model.uc.ResourceUC;
import org.enoch.snark.instance.service.ConditionChecker;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.service.ShipService;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.enoch.snark.instance.si.module.consumer.gi.SendFleetGIR;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.enoch.snark.action.command.FollowingAction.DELAY_TO_FLEET_BACK;
import static org.enoch.snark.action.command.FollowingAction.DELAY_TO_FLEET_THERE;
import static org.enoch.snark.action.command.status.ExecutionIssue.*;
import static org.enoch.snark.instance.model.to.ShipsMap.ALL_SHIPS;
import static org.enoch.snark.instance.si.module.consumer.gi.types.Mission.ATTACK;
import static org.enoch.snark.instance.si.module.consumer.gi.types.Mission.SPY;
import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class FleetSendProcessor {

    public static final Long TIME_BUFFER = 3L;

    private final ConditionChecker conditionChecker;
    private final TargetRepository targetRepository;
    private final PlayerRepository playerRepository;
    private final FleetRepository fleetRepository;
    private final ColonyRepository colonyRepository;
    private final ShipService shipService;

    public ExecutionIssue execute(GI gi, SendCommand command) {
        SendFleetGIR gir = new SendFleetGIR(gi);
        openSendFleetView(gi, command);
        if(!isValidated(command)) {
            command.getStatus().setSuccess();
            command.getStatus().setIssue(CONDITION_WONT_FIT);
            return NO_ISSUE;
        }
        ShipsMap shipsMap;
        if(ALL_SHIPS.equals(command.getShipsMap()) && (command.getLeaveShipsMap() == null || command.getLeaveShipsMap().isEmpty())) {
            shipsMap = ALL_SHIPS;
        } else {
            shipsMap = shipService.fromExpressionToValues(command.getShipsMap(), command.createFleetContext());
        }
        gir.selectShips(shipsMap);
        gir.next();
        gir.setSpeed(command.getSpeed());
        gir.fixDoNotWorkingDefaults(command);
        gir.setNewResources(command);

        FleetEntity fleet = new FleetEntity(command);

        long durationSeconds = gir.parseDurationSecounds().toSecondOfDay() + TIME_BUFFER;
        fleet.start = LocalDateTime.now();
        fleet.visited = gir.parseFleetVisited();
        fleet.back = gir.parseFleetBack();

        ExecutionIssue executionIssue = gir.sendFleet(fleet);
        fleetRepository.save(fleet);
        Navigator.getInstance().add(fleet);

        if(ATTACK.equals(fleet.mission)) {
            TargetEntity targetEntity = targetRepository.find(fleet.getTarget()).get();
            System.err.println("ATTACK "+targetEntity.toPlanet()+" spied "+timeAgo(targetEntity.lastSpiedOn)+" attacked "+timeAgo(targetEntity.lastAttacked));
        }


        if (SPY.equals(fleet.mission) && fleet.targetPosition!= 16) {
            Optional<TargetEntity> targetEntity = targetRepository.find(fleet.getTarget());
            if (targetEntity.isPresent()) {
                targetEntity.get().lastSpiedOn = fleet.visited;
                targetRepository.save(targetEntity.get());
            }
        }

        if (ATTACK.equals(fleet.mission)) {
            Optional<TargetEntity> targetEntity = targetRepository.find(fleet.getTarget());
            if (targetEntity.isPresent()) {
                targetEntity.get().lastAttacked = fleet.visited;
                targetRepository.save(targetEntity.get());
            }
        }

        updateDelayForAction(command, DELAY_TO_FLEET_THERE, durationSeconds);
        updateDelayForAction(command, DELAY_TO_FLEET_BACK, durationSeconds * 2);
        reloadColony(gi, command);

        if (NO_ISSUE.equals(executionIssue)) {
            command.getStatus().setSuccess();
        } else if (TO_WEAK_PLAYER.equals(executionIssue)) {
            TargetEntity target = targetRepository.byPlanet(command.getTarget().getPlanet());
            if (target != null) {
                PlayerEntity player = target.player;
                player.type = TargetEntity.WEAK;
                playerRepository.save(player);
            } else
                gi.url().openGalaxy(new SystemView(command.getTarget().getPlanet()), null);
            command.getStatus().setFailed(executionIssue);
        } else if (CAN_NOT_SENT.equals(executionIssue)) {
//            Planet target = new Planet(fleet.targetGalaxy, fleet.targetSystem, fleet.targetPosition);
            System.err.println("Can not send fleet " + command);
//            gi.url().openGalaxy(new SystemView(fleet.targetGalaxy, fleet.targetSystem), null);
//            instance.removePlanet(new Planet(fleet.getCoordinate()));
            if (fleet.code != null) fleet.code = -fleet.code;
            clearNext(command);
            command.getStatus().setFailed(executionIssue);
            return CAN_NOT_SENT;
        } else if (TO_WEAK_PLAYER.equals(executionIssue)) {
            clearNext(command);
            if (fleet.code != null) fleet.code = -fleet.code;
            command.getStatus().setFailed(executionIssue);
        } else {
            command.getStatus().setFailed(executionIssue);
        }
        return NO_ISSUE;
    }

    @Transactional
    private void openSendFleetView(GI gi, SendCommand command) {
        ColonyEntity colony = gi.url().openSendFleetView(command.getSource(), command.getTarget().getPlanet(), command.getMission());
        colonyRepository.save(colony);
    }

    private boolean isValidated(SendCommand command) {
        List<AbstractCondition> conditions = new ArrayList<>(command.getConditions());
        conditions.add(new FleetSlotCondition(1));
        conditions.add(new ShipsCondition(command.getShipsMap(), command.getLeaveShipsMap(), command.getSource().toPlanet()));
        if(!ResourceUC.isAbstractOrNull(command.getResources())) conditions.add(new ResourceCondition(command.getSource().toPlanet(), command.getResources(), command.getLeaveResources()));

        AbstractCondition wontFit = conditionChecker.check(conditions, command);
        if(wontFit != null) {
            String message = "Condition doesn't fit: " + wontFit;
//            throw new RuntimeException(message);
            System.err.println(message);
            return false;
        }
        return true;
    }

    public void clearNext(SendCommand command) {
        command.clearNext();
    }

    private void updateDelayForAction(SendCommand command, String action, Long durationSeconds) {
        if(command.isRequiredAction(action)) {
            command.getFollowingAction().setSecondsToDelay(durationSeconds);
        }
    }

    @Transactional
    private void reloadColony(GI gi, SendCommand command) {
        SleepUtil.pause();
        ColonyEntity colony = gi.url().openComponent(FLEETDISPATCH, command.getSource());
        colonyRepository.save(colony);
    }

    public static String timeAgo(LocalDateTime time) {
        if (time == null) {
            return "pierwszy raz";
        }

        Duration duration = Duration.between(time, LocalDateTime.now());
        long totalSeconds = duration.getSeconds();

        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0) sb.append(days).append(" dni ");
        if (hours > 0) sb.append(hours).append(" godz ");
        if (minutes > 0) sb.append(minutes).append(" min ");
        sb.append(seconds).append(" sek");

        return sb.toString().trim();
    }
}
