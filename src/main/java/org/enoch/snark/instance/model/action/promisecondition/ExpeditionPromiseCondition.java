package org.enoch.snark.instance.model.action.promisecondition;

import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.instance.model.to.FleetPromise;

public class ExpeditionPromiseCondition extends AbstractPromiseCondition {

    private final boolean check;

    public ExpeditionPromiseCondition(boolean check) {
        this.check = check;
    }

    @Override
    public boolean fit(FleetPromise colony) {
        throw new NotImplementedException("To fix in spring version");
//        return check && Consumer.getInstance().getExpeditionFreeSlots() > 0;
    }

    @Override
    public String reason(FleetPromise colony) {
        throw new NotImplementedException("To fix in spring version");
//        return "Expedition free should be > 0 and was "+ Consumer.getInstance().getExpeditionFreeSlots();
    }
}
