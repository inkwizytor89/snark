package org.enoch.snark.instance.model.action.condition;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.enoch.snark.instance.model.action.promisecondition.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
public abstract class AbstractCondition {

//    public abstract AbstractCondition check(ConditionContext context);
}
