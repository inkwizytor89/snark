package org.enoch.snark.instance.model.to;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.types.Mission;

import static org.enoch.snark.instance.model.to.ShipsMap.ALL_SHIPS;

public class FleetPromise {
    private ColonyEntity source;
    private Planet target;
    private Mission mission;
    private Long speed;

    private ShipsMap shipsMap;
    private Resources resources;

    private ShipsMap conditionShipsMap;
    private Resources conditionResources;
    private Long conditionResourcesCount;

    private ShipsMap leaveShipsMap;
    private Resources leaveResources;

    public FleetPromise() {
    }

    public boolean fit(ColonyEntity colony) {
        if(!colony.hasEnoughShips(conditionShipsMap)) {
            System.err.println(colony+" no fit for "+conditionShipsMap);
            return false;
        }

        if(!colony.hasEnoughResources(conditionResources)) {
            System.err.println(colony+" no fit for "+conditionResources);
            return false;
        }

        if(!colony.getResources().isCountMoreThan(conditionResourcesCount)) {
            System.err.println(colony+" not enough resource count "+conditionResources);
            return false;
        }
        return true;
    }

    public ShipsMap calculateShipMap(ColonyEntity colony) {
        ShipsMap sourceShipsMap = colony.getShipsMap();
        ShipsMap maxToSend = sourceShipsMap.leave(getLeaveShipsMap());
        ShipsMap shipsMap = getShipsMap();
        if(ALL_SHIPS.equals(getShipsMap()))
            shipsMap = maxToSend;
        return shipsMap.reduce(maxToSend);
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

    public ShipsMap getConditionShipsMap() {
        return conditionShipsMap;
    }

    public void setConditionShipsMap(ShipsMap conditionShipsMap) {
        this.conditionShipsMap = conditionShipsMap;
    }

    public Resources getConditionResources() {
        return conditionResources;
    }

    public void setConditionResources(Resources conditionResources) {
        this.conditionResources = conditionResources;
    }

    public Long getConditionResourcesCount() {
        return conditionResourcesCount;
    }

    public void setConditionResourcesCount(Long conditionResourcesCount) {
        this.conditionResourcesCount = conditionResourcesCount;
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
}
