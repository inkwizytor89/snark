package org.enoch.snark.instance.model.action.condition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.enoch.snark.instance.model.action.promisecondition.ConditionType;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.Resources;

import static org.enoch.snark.instance.model.action.promisecondition.ConditionType.RESOURCE_IN_SOURCE;

@RequiredArgsConstructor
@Getter
public class FleetSlotCondition extends AbstractCondition {
    private final int freeSlots;
}
