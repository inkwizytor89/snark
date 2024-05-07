package org.enoch.snark.instance.model.action.condition;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.model.to.Resources;

public class ResourceCondition extends AbstractCondition {
    private final Resources resources;

    public ResourceCondition(Resources resources) {
        this.resources = resources;
    }

    @Override
    public boolean fit(ColonyEntity colony) {
        return colony.hasEnoughResources(resources);
    }
}
