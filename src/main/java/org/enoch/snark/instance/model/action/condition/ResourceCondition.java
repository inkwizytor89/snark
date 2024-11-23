package org.enoch.snark.instance.model.action.condition;

import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Resources;

import static org.enoch.snark.instance.model.action.condition.ConditionType.RESOURCE_IN_SOURCE;
import static org.enoch.snark.instance.model.uc.ResourceUC.toTransport;

public class ResourceCondition extends AbstractCondition {
    public final ConditionType type =  RESOURCE_IN_SOURCE;
    private final Resources resources;

    public ResourceCondition(Resources resources) {
        this.resources = resources;
    }

    @Override
    public boolean fit(FleetPromise promise) {
//        return promise.getSource().hasEnoughResources(resources);
        return toTransport(promise.getSource(), resources, promise.getLeaveResources()) != null;
    }

    @Override
    public String reason(FleetPromise promise) {
        if(!fit(promise)) return promise.getSource() + " have " + promise.getSource().getResources() + " but needed " + resources;
        else return MISSING_REASON;
    }
}
