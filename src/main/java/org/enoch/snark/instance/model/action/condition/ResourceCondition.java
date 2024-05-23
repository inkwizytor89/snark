package org.enoch.snark.instance.model.action.condition;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.instance.model.to.Resources;

import static org.enoch.snark.instance.model.action.condition.ConditionType.RESOURCE_IN_SOURCE;

public class ResourceCondition extends AbstractCondition {
    public final ConditionType type =  RESOURCE_IN_SOURCE;
    private final Resources resources;

    public ResourceCondition(Resources resources) {
        this.resources = resources;
    }

    @Override
    public boolean fit(PlanetEntity colony) {
        return colony.hasEnoughResources(resources);
    }

    @Override
    public String reason(PlanetEntity colony) {
        if(!fit(colony)) return colony + " have " + colony.getResources() + " but needed " + resources;
        else return MISSING_REASON;
    }
}
