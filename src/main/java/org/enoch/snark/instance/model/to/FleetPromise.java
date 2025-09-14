package org.enoch.snark.instance.model.to;

import lombok.Data;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.enoch.snark.instance.model.action.promisecondition.AbstractPromiseCondition;
import org.enoch.snark.instance.model.uc.ShipUC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.model.uc.ShipUC.calculateShipCountForTransport;

@Data
@Deprecated
public class FleetPromise {
    private ColonyEntity source;
    private String target;
    private Mission mission;
    private Long speed;

    private ShipsMap shipsMap;
    private Resources resources;
    private final List<AbstractPromiseCondition> conditions = new ArrayList<>();
    private ShipsMap leaveShipsMap;
    private Resources leaveResources;

    public FleetPromise() {
    }

    public boolean fit() {
        return conditions.stream().allMatch(condition -> condition.fit(this));
    }

    public List<AbstractPromiseCondition> wontFit() {
        return conditions.stream().filter(condition -> !condition.fit(this)).collect(Collectors.toList());
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


    public void addCondition(AbstractPromiseCondition condition) {
        addConditions(Collections.singletonList(condition));
    }

    public void addConditions(List<AbstractPromiseCondition> conditions) {
        this.conditions.addAll(conditions);
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
                ", conditions=" + conditions +
                ", leaveShipsMap=" + leaveShipsMap +
                ", leaveResources=" + leaveResources +
                '}';
    }
}
