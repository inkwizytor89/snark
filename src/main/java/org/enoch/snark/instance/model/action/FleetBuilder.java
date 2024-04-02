package org.enoch.snark.instance.model.action;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.QueueRunType;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.ShipsMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.enoch.snark.db.entity.FleetEntity.FLEET_THREAD;

public class FleetBuilder {

    private List<ColonyEntity> from;
    private String to;
    private Mission mission;
    private List<ShipsMap> shipWaves;
    private Long speed;
    private Resources resources;
    private QueueRunType queue;

    public FleetBuilder from(ColonyEntity colony) {
        return from(Collections.singletonList(colony));
    }

    public FleetBuilder from(List<ColonyEntity> colonies) {
        from = colonies;
        return this;
    }

    public FleetBuilder to(String planet) {
        to = planet;
        return this;
    }

    public FleetBuilder mission(Mission mission) {
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

    public FleetBuilder resources(Resources resources) {
        this.resources = resources;
        return this;
    }

    public FleetBuilder speed(Long speed) {
        this.speed = speed;
        return this;
    }

    public FleetBuilder queue(QueueRunType queue) {
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
                FleetEntity fleetEntity = new FleetEntity();
                fleetEntity.source = colony;
                fleetEntity.setTarget(toExpression(colony, to));
                fleetEntity.mission = missionExpression(fleetEntity.getTarget(), mission);
                fleetEntity.setShips(shipsWave);
                fleetEntity.speed = speed;
                fleetEntity.code = FLEET_THREAD;

                SendFleetCommand command = new SendFleetCommand(fleetEntity);
                // move to promise in SendFleetCommand
                command.setResources(resources);
                command.setRunType(queue);
                command.generateHash(Integer.toString(index));

                results.add(command);
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

    private Planet toExpression(ColonyEntity colony, String to) {
        if(to != null) return new Planet(to);
        return colony.toPlanet().swapType();
    }

    private Mission missionExpression(Planet target, Mission mission) {
        if(mission != null) return mission;
        ColonyEntity colony = ColonyDAO.getInstance().get(target);
        if(colony != null) return Mission.STATIONED;
        return Mission.ATTACK;
    }

    @Override
    public String toString() {
        return "FleetBuilder{" +
                "sources=" + from +
                '}';
    }
}
