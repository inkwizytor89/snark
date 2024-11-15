package org.enoch.snark.instance.model.action;

import org.enoch.snark.common.time.Duration;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.command.impl.RecallCommand;
import org.enoch.snark.gi.command.impl.SendFleetPromiseCommand;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.QueueRunType;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.action.condition.ShipsCondition;
import org.enoch.snark.instance.model.action.filter.AbstractFilter;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.ShipsMap;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.enoch.snark.gi.types.Mission.TRANSPORT;
import static org.enoch.snark.instance.model.technology.Ship.transporterLarge;
import static org.enoch.snark.instance.model.action.PlanetExpression.asExpression;
import static org.enoch.snark.instance.model.action.PlanetExpression.toPlanetList;
import static org.enoch.snark.instance.model.to.Resources.nothing;
import static org.enoch.snark.instance.model.uc.ShipUC.calculateShipCountForTransport;

public class FleetBuilder {

    private List<ColonyEntity> from;
    private String to;
    private List<AbstractCondition> conditions = new ArrayList<>();
    private List<AbstractFilter> filters = new ArrayList<>();
    private Mission mission;
    private List<ShipsMap> shipWaves;
    private List<ShipsMap> leaveShipWaves;
    private Long speed;
    private Duration recall;
    private Resources resources;
    private Resources leaveResources;
    private QueueRunType queue;
    private String hashPrefix;

    public FleetBuilder from(ColonyEntity colony) {
        return from(singletonList(colony));
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

    public FleetBuilder addCondition(AbstractCondition condition) {
        if(condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    public FleetBuilder filters(List<AbstractFilter> filterList) {
        if(filterList != null) {
            filters = filterList;
        }
        return this;
    }

    public FleetBuilder mission(Mission mission) {
        if(mission!= null && !Mission.UNKNOWN.equals(mission))
            this.mission = mission;
        return this;
    }

    public FleetBuilder ships(ShipsMap shipsMap) {
        return ships(singletonList(shipsMap));
    }

    public FleetBuilder ships(List<ShipsMap> shipsMaps) {
        shipWaves = shipsMaps;
        return this;
    }

    public FleetBuilder leaveShips(ShipsMap leaveShips) {
        return leaveShips(singletonList(leaveShips));
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
        if(leaveResources != null)
            this.leaveResources = leaveResources;
        return this;
    }

    public FleetBuilder speed(Long speed) {
        this.speed = speed;
        return this;
    }

    public FleetBuilder recall(Duration recall) {
        this.recall = recall;
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

    private List<SendFleetPromiseCommand> build() {
        defaultValues();
        validate();

        List<SendFleetPromiseCommand> results = new ArrayList<>();
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

                    SendFleetPromiseCommand command = new SendFleetPromiseCommand(promise);
                    commandPromiseSetLeaveShipsMap(command, index);
                    command.setRunType(queue);
                    command.generateHash(hashPrefix, Integer.toString(index));
                    if(recall != null) command.setNext(new RecallCommand(promise), recall.getSeconds());

                    results.add(command);
                }
            }
        }
        return results;
    }

    public SendFleetPromiseCommand buildOne() {
        return buildAll().get(0);
    }

    public List<SendFleetPromiseCommand> buildAll() {
        List<SendFleetPromiseCommand> built = build();
        for (AbstractFilter filter : filters) {
            built = filter.filter(built);
        }
        return built;
    }

    private void defaultValues() {
        if(from == null) from = Instance.getSources();
        if(TRANSPORT.equals(mission) && resources != null && shipWaves == null) {
            ShipsMap transportShipsMap = new ShipsMap();
            transportShipsMap.put(transporterLarge, calculateShipCountForTransport(transporterLarge, resources));

            addCondition(new ShipsCondition(transportShipsMap));
            shipWaves = singletonList(transportShipsMap);
        }
    }

    private void validate() {
        //main source maybe
        if(from.isEmpty()) throw new RuntimeException("Missing source for "+this);
    }

    private Mission missionExpression(Planet target, Mission mission) {
        if(mission != null) return mission;
        ColonyEntity colony = ColonyDAO.getInstance().find(target);
        if(colony != null) return Mission.STATIONED;
        return Mission.ATTACK;
    }

    public void commandPromiseSetLeaveShipsMap(SendFleetPromiseCommand command, int index) {
        if(leaveShipWaves != null && !leaveShipWaves.isEmpty()) {
            command.promise().setLeaveShipsMap(leaveShipWaves.get(Math.min(index, leaveShipWaves.size()-1)));
        }
    }
}
