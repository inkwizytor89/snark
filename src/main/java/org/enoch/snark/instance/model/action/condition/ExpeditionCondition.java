package org.enoch.snark.instance.model.action.condition;

import org.enoch.snark.instance.commander.Commander;
import org.enoch.snark.instance.model.to.FleetPromise;

public class ExpeditionCondition extends AbstractCondition {

    private final boolean check;

    public ExpeditionCondition(boolean check) {
        this.check = check;
    }

    @Override
    public boolean fit(FleetPromise colony) {
        return check && Commander.getInstance().getExpeditionFreeSlots() > 0;
    }

    @Override
    public String reason(FleetPromise colony) {
        return "Expedition free should be > 0 and was "+Commander.getInstance().getExpeditionFreeSlots();
    }
}
