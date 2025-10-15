package org.enoch.snark.instance.model.action.condition;

import lombok.Getter;
import org.enoch.snark.instance.model.to.Planet;

@Getter
public abstract class ExpiredCondition extends AbstractCondition {
    protected Long seconds;
    protected Boolean is;
    protected Planet target;

}
