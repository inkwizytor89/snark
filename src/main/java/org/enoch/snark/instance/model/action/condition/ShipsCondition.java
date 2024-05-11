package org.enoch.snark.instance.model.action.condition;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.ShipsMap;

public class ShipsCondition extends AbstractCondition {
    private final ShipsMap shipsMap;

    public ShipsCondition(ShipsMap shipsMap) {
        this.shipsMap = shipsMap;
    }

    @Override
    public boolean fit(ColonyEntity colony) {
        return colony.hasEnoughShips(shipsMap);
    }

    @Override
    public String reason(ColonyEntity colony) {
        if(!fit(colony)) return colony + " have " + colony.getShipsMap() + " but needed " + shipsMap;
        else return MISSING_REASON;
    }
}
