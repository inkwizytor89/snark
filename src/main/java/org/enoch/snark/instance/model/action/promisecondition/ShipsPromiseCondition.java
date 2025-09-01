package org.enoch.snark.instance.model.action.promisecondition;

import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.model.uc.ShipUC;

public class ShipsPromiseCondition extends AbstractPromiseCondition {
    private final ShipsMap shipsMap;

    public ShipsPromiseCondition(ShipsMap shipsMap) {
        this.shipsMap = shipsMap;
    }

    @Override
    public boolean fit(FleetPromise promise) {
        ShipsMap valuedMap = ShipUC.fromExpressionToValues(shipsMap, promise);
        return promise.getSource().hasEnoughShips(valuedMap);
    }

    @Override
    public String reason(FleetPromise promise) {
        if(!fit(promise)) return promise.getSource() + " have " + promise.getSource().getShipsMap() + " but needed " + shipsMap;
        else return MISSING_REASON;
    }
}
