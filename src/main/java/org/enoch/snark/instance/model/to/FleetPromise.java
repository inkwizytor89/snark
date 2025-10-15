package org.enoch.snark.instance.model.to;

import lombok.Data;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.enoch.snark.instance.model.uc.ShipUC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Data
@Deprecated
public class FleetPromise {
    private ColonyEntity source;
    private String target;
    private Mission mission;
    private Long speed;

    private ShipsMap shipsMap;
    private Resources resources;
    private ShipsMap leaveShipsMap;
    private Resources leaveResources;

    public FleetPromise() {
    }


    public ShipsMap normalizeShipMap() {
        return ShipUC.fromExpressionToValues(shipsMap, this);

//        return shipsMap.reduce(maxToSend);
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setTarget(Planet target) {
        this.target = target.toString();
    }


    @Override
    public String toString() {
        return "FleetPromise{" +
                "source=" + source +
                ", target=" + target +
                ", mission=" + mission +
                ", speed=" + speed +
                ", shipsMap=" + shipsMap +
                ", resources=" + resources +
                ", leaveShipsMap=" + leaveShipsMap +
                ", leaveResources=" + leaveResources +
                '}';
    }
}
