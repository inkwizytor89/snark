package org.enoch.snark.instance.model.to;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.uc.ShipUC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.model.to.ShipsMap.ALL_SHIPS;
import static org.enoch.snark.instance.model.to.ShipsMap.TRANSPORT_COUNT;
import static org.enoch.snark.instance.model.uc.ShipUC.calculateShipCountForTransport;

public class FleetPromise {
    private ColonyEntity source;
    private Planet target;
    private Mission mission;
    private Long speed;

    private ShipsMap shipsMap;
    private Resources resources;
    private final List<AbstractCondition> conditions = new ArrayList<>();
    private ShipsMap leaveShipsMap;
    private Resources leaveResources;

    public FleetPromise() {
    }

    public boolean fit() {
        return conditions.stream().allMatch(condition -> condition.fit(this));
    }

    public List<AbstractCondition> wontFit() {
        return conditions.stream().filter(condition -> !condition.fit(this)).collect(Collectors.toList());
    }

    public ShipsMap normalizeShipMap() {
        return ShipUC.fromExpressionToValues(shipsMap, this);

//        return shipsMap.reduce(maxToSend);
    }

    public ShipsMap getShipsMap() {
        return shipsMap;
    }

    public void setShipsMap(ShipsMap shipsMap) {
        this.shipsMap = shipsMap;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public ShipsMap getLeaveShipsMap() {
        return leaveShipsMap;
    }

    public void setLeaveShipsMap(ShipsMap leaveShipsMap) {
        this.leaveShipsMap = leaveShipsMap;
    }

    public Resources getLeaveResources() {
        return leaveResources;
    }

    public void setLeaveResources(Resources leaveResources) {
        this.leaveResources = leaveResources;
    }

    public ColonyEntity getSource() {
        return source;
    }

    public void setSource(ColonyEntity source) {
        this.source = source;
    }

    public Planet getTarget() {
        return target;
    }

    public void setTarget(Planet target) {
        this.target = target;
    }

    public Mission getMission() {
        return mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    public Long getSpeed() {
        return speed;
    }

    public void setSpeed(Long speed) {
        this.speed = speed;
    }

    public void addCondition(AbstractCondition condition) {
        addConditions(Collections.singletonList(condition));
    }

    public void addConditions(List<AbstractCondition> conditions) {
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
