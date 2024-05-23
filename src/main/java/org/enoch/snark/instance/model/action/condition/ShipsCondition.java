package org.enoch.snark.instance.model.action.condition;

import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.instance.model.to.ShipsMap;

public class ShipsCondition extends AbstractCondition {
    private final ShipsMap shipsMap;

    public ShipsCondition(ShipsMap shipsMap) {
        this.shipsMap = shipsMap;
    }

    @Override
    public boolean fit(PlanetEntity colony) {
        return colony.hasEnoughShips(shipsMap);
    }

    @Override
    public String reason(PlanetEntity colony) {
        if(!fit(colony)) return colony + " have " + colony.getShipsMap() + " but needed " + shipsMap;
        else return MISSING_REASON;
    }
}
