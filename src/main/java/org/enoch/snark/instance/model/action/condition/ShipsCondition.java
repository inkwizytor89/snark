package org.enoch.snark.instance.model.action.condition;

import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.ShipsMap;

public class ShipsCondition extends AbstractCondition {
    private final ShipsMap shipsMap;

    public ShipsCondition(ShipsMap shipsMap) {
        this.shipsMap = shipsMap;
    }

    @Override
    public boolean fit(FleetPromise promise) {
        return promise.getSource().hasEnoughShips(shipsMap);
    }

    @Override
    public String reason(FleetPromise promise) {
        if(!fit(promise)) return promise.getSource() + " have " + promise.getSource().getShipsMap() + " but needed " + shipsMap;
        else return MISSING_REASON;
    }
}
