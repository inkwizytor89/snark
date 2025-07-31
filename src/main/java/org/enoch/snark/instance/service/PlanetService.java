package org.enoch.snark.instance.service;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.FleetSendCommand;
import org.enoch.snark.action.command.SendFleetCommand;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Planet;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class PlanetService {
    private final ColonyRepository colonyRepository;

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
                command.setTarget(target);
                command.setMission(promise.getMission());
                command.setSpeed(promise.getSpeed());
                command.setShipsMap(promise.getShipsMap());
                command.setResources(promise.getResources());
                command.addConditions(promise.getConditions());
                command.setLeaveShipsMap(promise.getLeaveShipsMap());
                command.setLeaveResources(promise.getLeaveResources());
                command.setPromise(promise);
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
