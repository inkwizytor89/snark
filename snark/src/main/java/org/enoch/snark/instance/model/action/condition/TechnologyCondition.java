package org.enoch.snark.instance.model.action.condition;

import lombok.Getter;
import org.enoch.snark.instance.model.technology.Technology;
import org.enoch.snark.instance.model.to.Planet;

@Getter
public abstract class TechnologyCondition extends AbstractCondition {
    protected Technology technology;
    protected Planet source; // moze zeby tu wchodziło expresion
    protected Long level;
    protected String type;

}
