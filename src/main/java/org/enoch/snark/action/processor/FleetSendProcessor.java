package org.enoch.snark.action.processor;

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
import org.enoch.snark.instance.model.action.condition.ResourceInSourceCondition;
import org.enoch.snark.instance.model.action.condition.ShipsCondition;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.SystemView;
import org.enoch.snark.instance.service.ConditionChecker;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.enoch.snark.instance.si.module.consumer.gi.SendFleetGIR;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.enoch.snark.action.command.FollowingAction.DELAY_TO_FLEET_BACK;
import static org.enoch.snark.action.command.FollowingAction.DELAY_TO_FLEET_THERE;
import static org.enoch.snark.action.command.status.ExecutionIssue.*;
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

    public boolean execute(GI gi, SendCommand command) {
        SendFleetGIR gir = new SendFleetGIR(gi);
        ColonyEntity colony = gi.url().openSendFleetView(command.getSource(), command.getTarget(), command.getMission());
        colonyRepository.save(colony);
        validate(command);
        gir.selectShips(command);
        gir.next();
        gir.setSpeed(command.getSpeed());
        gir.setNewResources(command);

        FleetEntity fleet = new FleetEntity(command);

        long durationSeconds = gir.parseDurationSecounds().toSecondOfDay() + TIME_BUFFER;
        fleet.start = LocalDateTime.now();
        fleet.visited = gir.parseFleetVisited();
        fleet.back = gir.parseFleetBack();


        ExecutionIssue executionIssue = gir.sendFleet(fleet);
        if(NO_ISSUE.equals(executionIssue)) {
            command.getStatus().setSuccess();
        } else if(TO_WEAK_PLAYER.equals(executionIssue)) {
            TargetEntity target = targetRepository.byPlanet(command.getTarget());
            if (target != null) {
                PlayerEntity player = target.player;
                player.type = TargetEntity.WEAK;
                playerRepository.save(player);
            }
            else gi.url().openGalaxy(new SystemView(command.getTarget().galaxy, command.getTarget().system), null);
            command.getStatus().setFailed(executionIssue);
        } else if(CAN_NOT_SENT.equals(executionIssue)) {
            Planet target = new Planet(fleet.targetGalaxy, fleet.targetSystem, fleet.targetPosition);
            System.err.println("Can not send fleet to target " + target);
            gi.url().openGalaxy(new SystemView(fleet.targetGalaxy, fleet.targetSystem), null);
//            instance.removePlanet(new Planet(fleet.getCoordinate()));
            if(fleet.code != null) fleet.code = - fleet.code;
            clearNext(command);
            command.getStatus().setFailed(executionIssue);
            return true;
        } else if(TO_WEAK_PLAYER.equals(executionIssue)) {
            clearNext(command);
            if(fleet.code != null) fleet.code = -fleet.code;
            command.getStatus().setFailed(executionIssue);
        } else {
            command.getStatus().setFailed(executionIssue);
        }

        fleetRepository.save(fleet);
        Navigator.getInstance().add(fleet);

        if(SPY.equals(fleet.mission)) {
            TargetEntity targetEntity = targetRepository.byPlanet(fleet.getTarget());
            if(targetEntity != null) {
                targetEntity.lastSpiedOn = fleet.visited;
                targetRepository.save(targetEntity);
            }
        }

        if(ATTACK.equals(fleet.mission)) {
            TargetEntity targetEntity = targetRepository.byPlanet(fleet.getTarget());
            if(targetEntity != null) {
                targetEntity.lastAttacked = fleet.visited;
                targetRepository.save(targetEntity);
            }
        }

        updateDelayForAction(command, DELAY_TO_FLEET_THERE, durationSeconds);
        updateDelayForAction(command, DELAY_TO_FLEET_BACK, durationSeconds*2);
        reloadColony(gi, command);

        return true;
    }

    private void validate(SendCommand command) {
        List<AbstractCondition> conditions = new ArrayList<>(command.getConditions());
        conditions.add(new FleetSlotCondition(1));
        conditions.add(new ShipsCondition(command.getShipsMap(), command.getLeaveShipsMap(), command.getSource().toPlanet()));
        conditions.add(new ResourceInSourceCondition(command.getSource().toPlanet(), command.getResources(), command.getLeaveResources()));

        AbstractCondition wontFit = conditionChecker.check(conditions);
        if(wontFit != null) throw new RuntimeException(wontFit.toString());
    }

    public void clearNext(SendCommand command) {
        command.clearNext();
    }

    private void updateDelayForAction(SendCommand command, String action, Long durationSeconds) {
        if(command.isRequiredAction(action)) {
            command.getFollowingAction().setSecondsToDelay(durationSeconds);
        }
    }

    private void reloadColony(GI gi, SendCommand command) {
        SleepUtil.sleep();
        ColonyEntity colony = gi.url().openComponent(FLEETDISPATCH, command.getSource());
        colonyRepository.save(colony);
    }
}
