package org.enoch.snark.instance.service;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.SendCommand;
import org.enoch.snark.instance.model.action.condition.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class ConditionChecker {

    private final ConditionContext conditionContext;

    public boolean fit(List<AbstractCondition> conditions, SendCommand command) {
        return check(conditions, command) == null;
    }

    public AbstractCondition check(List<AbstractCondition> conditions, SendCommand command) {
        if(conditions == null) return null;
        for (AbstractCondition condition : conditions) {
            AbstractCondition unfulfilled = check(condition, command);
            if (unfulfilled != null) return unfulfilled;
        }
        return null;
    }

    public AbstractCondition check(AbstractCondition abstractCondition, SendCommand command) {
        conditionContext.setCommand(command);
        return switch (abstractCondition) {
            case HasResourcesCondition condition -> condition.check(conditionContext);
            case HasResourcesCountCondition condition -> condition.check(conditionContext);
            case IsSpyNotExpiredCondition condition -> condition.check(conditionContext);
            case IsAttackNotExpiredCondition condition -> condition.check(conditionContext);
            case AreShipsCondition condition -> condition.check(conditionContext);
            case HasNoMissionsCondition condition -> condition.check(conditionContext);
            case HasFleetSlotsCondition condition -> condition.check(conditionContext);
            case IsExpressionTrueCondition condition -> condition.check(conditionContext);
            case IsModifiedAfterCondition condition -> condition.check(conditionContext);
            default -> throw new IllegalStateException("Unknown condition " + abstractCondition.getClass().getName());
        };
    }
}
