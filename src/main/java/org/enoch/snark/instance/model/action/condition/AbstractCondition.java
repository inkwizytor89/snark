package org.enoch.snark.instance.model.action.condition;

import org.enoch.snark.db.entity.ColonyEntity;

public abstract class AbstractCondition {

    protected static final String MISSING_REASON = "Missing reason";

    public abstract boolean fit(ColonyEntity colony);

    public abstract String reason(ColonyEntity colony);
}
