package org.enoch.snark.instance.model.action.condition;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
public abstract class AbstractCondition {

    public abstract AbstractCondition check(ConditionContext context);

}
