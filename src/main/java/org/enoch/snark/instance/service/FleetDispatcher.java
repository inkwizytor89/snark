package org.enoch.snark.instance.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.action.command.FleetSendCommand;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.model.to.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class FleetDispatcher {
    private final PlanetService planetService;

    public List<FleetSendCommand> from(FleetPlan fleetPlan) {
        List<FleetSendCommand> list = new ArrayList<>();
        int index = 0;
        for(ShipsMap shipsWave : fleetPlan.getShipsWaves()) {
            index++;

            List<PlanetData> colonies = planetService.fromExpression(fleetPlan.getSource());
            for (PlanetData colony : colonies) {
                FleetContext fleetContext = FleetContext.builder()
                        .trip(fleetPlan.getTrip())
                        .source(colony)
                        .build();

                List<PlanetData> targets = planetService.fromExpression(fleetPlan.getTarget(), fleetContext);
                for(PlanetData target : targets) {
                    FleetSendCommand command = new FleetSendCommand();
                    command.setSource(colony.getColony());
                    command.setTarget(target);
                    command.setMission(fleetPlan.getMission());
                    command.setSpeed(fleetPlan.getSpeed());
                    command.setShipsMap(shipsWave);
                    command.setResources(fleetPlan.getResources());
                    command.addConditions(fleetPlan.getConditions());
                    command.setLeaveShipsMap(calculateIndexedShipWave(fleetPlan.getLeaveShips(), index));
                    command.setLeaveResources(fleetPlan.getLeaveResources());
                    command.setFleetPlan(fleetPlan);
                    list.add(command);
                }
            }
        }
        return list;
    }

    private static ShipsMap calculateIndexedShipWave(List<ShipsMap> shipsWave, int index) {
        if(shipsWave == null) return ShipsMap.NO_SHIPS;
        int size = shipsWave.size();
        if(size > 1) throw new NotImplementedException("ShipsWave more than 1 not implemented already");

//        if(shipsWave != null && !shipsWave.isEmpty()) {
//            return shipsWave.get(Math.min(index, size-1));
//        }
        return size == 1 ? shipsWave.getFirst() : null;
    }

    public List<Planet> from(String input) {
        return Planet.fromString(input);
    }

//    musi byc tak że promise to jest abstrakcyjny twór oparty na stringach tak target
//            i moze misja, i jakas klasa np ta transferuje to na zbiór realnych command ktore sa jednoznaczne
//            bo builder nie ma dostepu do bazy danych i nie jest w stanie przetworzyc wyrazenia na konkretne planety lub misje lub inne
//
    public List<FleetSendCommand> from(FleetPromise promise) {
        List<FleetSendCommand> list = new ArrayList<>();
        if(promise != null) {
            for(Planet target : from(promise.getTarget())) {
                FleetSendCommand command = new FleetSendCommand();
                command.setSource(promise.getSource());
                command.setTarget(new PlanetData(new TargetEntity(target)));
                command.setMission(promise.getMission());
                command.setSpeed(promise.getSpeed());
                command.setShipsMap(promise.getShipsMap());
                command.setResources(promise.getResources());
//                command.addConditions(promise.getConditions());
                command.setLeaveShipsMap(promise.getLeaveShipsMap());
                command.setLeaveResources(promise.getLeaveResources());
//                command.setFleetPlan(promise);
                list.add(command);
            }
        }
        return list;
    }

    public List<FleetSendCommand> from(List<FleetPromise> promises) {
        List<FleetSendCommand> list = new ArrayList<>();
        for(FleetPromise promise : promises) {
            list.addAll(from(promise));
        }
        return list;
    }
}
