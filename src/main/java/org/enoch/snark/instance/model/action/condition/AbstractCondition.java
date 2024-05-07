package org.enoch.snark.instance.model.action.condition;

import org.enoch.snark.db.entity.ColonyEntity;

public abstract class AbstractCondition {

    public abstract boolean fit(ColonyEntity colony);
}
