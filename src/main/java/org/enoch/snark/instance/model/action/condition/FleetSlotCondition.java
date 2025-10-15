package org.enoch.snark.instance.model.action.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.enoch.snark.instance.service.Navigator;

@Getter
public class FleetSlotCondition extends AbstractCondition {
    private final int freeSlots;

    @JsonCreator
    public FleetSlotCondition(
            @JsonProperty("freeSlots") Integer freeSlots
    ) {
        this.freeSlots = freeSlots;
    }

    @Override
    public AbstractCondition check(ConditionContext context) {
        int fleetFreeSlots = Navigator.getFleetFreeSlots();
        boolean isPossible = getFreeSlots() <= fleetFreeSlots;
        return isPossible ? null : this;
    }
}
