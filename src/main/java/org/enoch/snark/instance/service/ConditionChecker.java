package org.enoch.snark.instance.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.TargetRepository;
import org.enoch.snark.instance.model.action.condition.*;
import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.model.uc.ShipUC;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;

import static org.enoch.snark.instance.model.to.Resources.nothing;
import static org.enoch.snark.instance.model.uc.ResourceUC.toTransport;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class ConditionChecker {
    private final ColonyRepository colonyRepository;
    private final TargetRepository targetRepository;

    public boolean fit(List<AbstractCondition> conditions) {
        return check(conditions) == null;
    }

    public AbstractCondition check(List<AbstractCondition> conditions) {
        if(conditions == null) return null;
        for (AbstractCondition condition : conditions) {
            AbstractCondition unfulfilled = check(condition);
            if (unfulfilled != null) return unfulfilled;
        }
        return null;
    }

    public AbstractCondition check(AbstractCondition abstractCondition) {
        return switch (abstractCondition) {
            case ResourceInSourceCondition condition -> check(condition);
            case ResourceCountCondition condition -> check(condition);
            case SpyNotExpiredCondition condition -> check(condition);
            case AttackNotExpiredCondition condition -> check(condition);
            case ShipsCondition condition -> check(condition);
            case NoMissionsCondition condition -> check(condition);
            case FleetSlotCondition condition -> check(condition);
            default -> throw new IllegalStateException("Unknown condition " + abstractCondition.getClass().getName());
        };
    }

    private AbstractCondition check(ResourceInSourceCondition condition) {
        if(nothing.equals(condition.getResources())) return null;
        ColonyEntity colonyEntity = colonyRepository.byPlanet(condition.getSource());
        boolean isPossible = toTransport(colonyEntity, condition.getResources(), condition.getLeaveResources()) != null;
        return isPossible ? null : condition;
    }

    private AbstractCondition check(ResourceCountCondition condition) {
        TargetEntity target = targetRepository.byPlanet(condition.getPlanet());
        boolean isPossible = target.getResources().isCountMoreThan(condition.getResourcesCount());
        return isPossible ? null : condition;
    }

    private AbstractCondition check(SpyNotExpiredCondition condition) {
        TargetEntity target = targetRepository.byPlanet(condition.getPlanet());
        boolean isExpired = DateUtil.isExpired(target.lastSpiedOn, condition.getSeconds(), ChronoUnit.SECONDS);
        boolean isPossible = condition.getIs() == isExpired;
        return isPossible ? null : condition;
    }

    private AbstractCondition check(AttackNotExpiredCondition condition) {
        TargetEntity target = targetRepository.byPlanet(condition.getPlanet());
        boolean isExpired = DateUtil.isExpired(target.lastAttacked, condition.getSeconds(), ChronoUnit.SECONDS);
        boolean isPossible = condition.getIs() == isExpired;
        return isPossible ? null : condition;
    }

    private AbstractCondition check(ShipsCondition condition) {
        ColonyEntity colonyEntity = colonyRepository.byPlanet(condition.getSource());
//        throw new NotImplementedException("ShipUC.fromExpressionToValues not implemented without promise");
        ShipsMap valuedMap = ShipUC.fromExpressionToValues(condition.getShipsMap(), colonyEntity, condition.getLeave());
        boolean isPossible = colonyEntity.hasEnoughShips(valuedMap);
        return isPossible ? null : condition;
    }

    private AbstractCondition check(NoMissionsCondition condition) {
        ColonyEntity colonyEntity = colonyRepository.byPlanet(condition.getSource());
        if(condition.getBlockingMissions() == null) return null;
        List<EventFleet> blockedFleets = Navigator.getInstance().getEventFleetList().stream()
                .filter(fleet -> inAny(fleet.mission, condition.getBlockingMissions()))
                .filter(fleet -> colonyEntity.toPlanet().equals(fleet.getEndingPlanet()))
                .toList();
        boolean isPossible = blockedFleets.isEmpty();
        return isPossible ? null : condition;
    }

    private AbstractCondition check(FleetSlotCondition condition) {
        int fleetFreeSlots = Navigator.getFleetFreeSlots();
        boolean isPossible = condition.getFreeSlots() <= fleetFreeSlots;
        return isPossible ? null : condition;
    }

    private boolean inAny(Mission mission, List<Mission> blockingMissions) {
        if(blockingMissions == null) return true;
        return blockingMissions.stream().anyMatch(blockingMission -> blockingMission.equals(mission));
    }
}
