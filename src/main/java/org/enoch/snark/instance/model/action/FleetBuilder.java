package org.enoch.snark.instance.model.action;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.QueueRunType;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.ShipsMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.enoch.snark.instance.model.action.PlanetExpression.asExpression;
import static org.enoch.snark.instance.model.action.PlanetExpression.toPlanetList;
import static org.enoch.snark.instance.model.to.Resources.nothing;

public class FleetBuilder {

    private List<ColonyEntity> from;
    private String to;
    private List<AbstractCondition> conditions = new ArrayList<>();
    private Mission mission;
    private List<ShipsMap> shipWaves;
    private List<ShipsMap> leaveShipWaves;
    private Long speed;
    private Resources resources;
    private Resources leaveResources;
    private QueueRunType queue;
    private String hashPrefix;

    public FleetBuilder from(ColonyEntity colony) {
        return from(Collections.singletonList(colony));
    }

    public FleetBuilder from(String expression) {
        return from(PlanetExpression.toColonies(expression));
    }

    public FleetBuilder from(List<ColonyEntity> colonies) {
        from = colonies;
        return this;
    }

    public FleetBuilder to(String planet) {
        to = planet;
        return this;
    }

    public FleetBuilder conditions(List<AbstractCondition> conditionList) {
        if(conditionList != null) {
            conditions = conditionList;
        }
        return this;
    }

    public FleetBuilder mission(Mission mission) {
        if(mission!= null && !Mission.UNKNOWN.equals(mission))
            this.mission = mission;
        return this;
    }

    public FleetBuilder ships(ShipsMap shipsMap) {
        return ships(Collections.singletonList(shipsMap));
    }

    public FleetBuilder ships(List<ShipsMap> shipsMaps) {
        shipWaves = shipsMaps;
        return this;
    }

    public FleetBuilder leaveShips(ShipsMap leaveShips) {
        return leaveShips(Collections.singletonList(leaveShips));
    }

    public FleetBuilder leaveShips(List<ShipsMap> leaveShipWaves) {
        if(leaveShipWaves != null && !leaveShipWaves.isEmpty())
            this.leaveShipWaves = leaveShipWaves;
        return this;
    }

    public FleetBuilder resources(Resources resources) {
        if(!nothing.equals(resources))
            this.resources = resources;
        return this;
    }

    public FleetBuilder leaveResources(Resources leaveResources) {
        if(!nothing.equals(resources))
            this.leaveResources = leaveResources;
        return this;
    }

    public FleetBuilder speed(Long speed) {
        this.speed = speed;
        return this;
    }

    public FleetBuilder hashPrefix(String hashPrefix) {
        if(hashPrefix!= null && !hashPrefix.isEmpty())
            this.hashPrefix = hashPrefix;
        return this;
    }

    public FleetBuilder queue(QueueRunType queue) {
        if(queue != null)
            this.queue = queue;
        return this;
    }

    public List<SendFleetCommand> buildAll() {
        defaultValues();
        validate();

        List<SendFleetCommand> results = new ArrayList<>();
        int index = 0;
        for(ShipsMap shipsWave : shipWaves) {
            index++;
            for (ColonyEntity colony : from) {
                List<Planet> targets = toPlanetList(asExpression(colony, to));
                if(targets==null) continue;
                for(Planet target : targets) {
                    if (target == null) continue;
                    FleetPromise promise = new FleetPromise();
                    promise.setSource(colony);
                    promise.setTarget(target);
                    promise.setMission(missionExpression(target, mission));
                    promise.setShipsMap(shipsWave);
                    promise.setSpeed(speed);

                    promise.setResources(resources);
                    promise.setLeaveResources(leaveResources);
                    promise.addConditions(conditions);

                    SendFleetCommand command = new SendFleetCommand(promise);
                    commandPromiseSetLeaveShipsMap(command, index);
                    command.setRunType(queue);
                    command.generateHash(hashPrefix, Integer.toString(index));

                    results.add(command);
                }
            }
        }
        return results;
    }

    public SendFleetCommand buildOne() {
        return buildAll().get(0);
    }

    private void defaultValues() {
        if(from == null) from = Instance.getSources();
    }

    private void validate() {
        //main source maybe
        if(from.isEmpty()) throw new RuntimeException("Missing source for "+this);
    }

    private Mission missionExpression(Planet target, Mission mission) {
        if(mission != null) return mission;
        ColonyEntity colony = ColonyDAO.getInstance().get(target);
        if(colony != null) return Mission.STATIONED;
        return Mission.ATTACK;
    }

    public void commandPromiseSetLeaveShipsMap(SendFleetCommand command, int index) {
        if(leaveShipWaves != null && !leaveShipWaves.isEmpty()) {
            command.promise().setLeaveShipsMap(leaveShipWaves.get(Math.min(index, leaveShipWaves.size()-1)));
        }
    }
}
