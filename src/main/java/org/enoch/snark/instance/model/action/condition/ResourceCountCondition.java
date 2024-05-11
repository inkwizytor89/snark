package org.enoch.snark.instance.model.action.condition;

import org.enoch.snark.db.entity.ColonyEntity;

public class ResourceCountCondition extends AbstractCondition {
    private final Long resourcesCount;

    public ResourceCountCondition(Long resourcesCount) {
        this.resourcesCount = resourcesCount;
    }

    @Override
    public boolean fit(ColonyEntity colony) {
        return colony.getResources().isCountMoreThan(resourcesCount.toString());
    }

    @Override
    public String reason(ColonyEntity colony) {
        if(!fit(colony)) return colony + " have " + colony.getResources() + " but count is needed " + resourcesCount;
        else return MISSING_REASON;
    }
}
