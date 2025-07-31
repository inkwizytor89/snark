package org.enoch.snark.instance.model.action;

import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.action.command.RecallCommand;
import org.enoch.snark.action.command.SendFleetPromiseCommand;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.si.QueueRunType;
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
import static org.enoch.snark.instance.si.module.consumer.gi.types.Mission.TRANSPORT;
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
    private Resources resources;
    private Resources leaveResources;

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

    private List<FleetPromise> build() {
        defaultValues();
        validate();

        List<FleetPromise> results = new ArrayList<>();
        int index = 0;
        for(ShipsMap shipsWave : shipWaves) {
            index++;
            for (ColonyEntity colony : from) {
//                List<Planet> targets = toPlanetList(asExpression(colony, to));
//                if(targets==null) continue;
//                for(Planet target : targets) {
                    if (to == null) continue;
                    FleetPromise promise = new FleetPromise();
                    promise.setSource(colony);

//                    target nie moze byc planeta, tu musi byc strin lub jakas klasa do wyrazenia
//                    FleetThreed w promisie powinien miec cos takiego i wtedy zawolac beana ktory ma dostep do bazy danych
//                    albo innych klaso beanow ktore beda mogły przetransformowac to do planety dopiero

                    promise.setTarget(to);
                    promise.setMission(missionExpression(to, mission));
                    promise.setShipsMap(shipsWave);
                    promise.setSpeed(speed);

                    promise.setResources(resources);
                    promise.setLeaveResources(leaveResources);
                    promise.addConditions(conditions);

                    if(leaveShipWaves != null && !leaveShipWaves.isEmpty()) {
                        promise.setLeaveShipsMap(leaveShipWaves.get(Math.min(index, leaveShipWaves.size()-1)));
                    }

                    results.add(promise);
//                }
            }
        }
        return results;
    }

    public FleetPromise buildOne() {
        return buildAll().get(0);
    }

    public List<FleetPromise> buildAll() {
        List<FleetPromise> built = build();
        if(!filters.isEmpty()) throw new RuntimeException("Filters not implemented yet");
//        for (AbstractFilter filter : filters) {
//            built = filter.filter(built);
//        }
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

    private Mission missionExpression(String target, Mission mission) {
        if(mission != null) return mission;
        throw new NotImplementedException("Default mission need fix");
//        ColonyEntity colony = ColonyDAO.getInstance().find(target);
//        if(colony != null) return Mission.STATIONED;
//        return Mission.ATTACK;
    }
}
