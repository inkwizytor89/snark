package org.enoch.snark.instance.service;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.FleetSendCommand;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.model.action.condition.ResourceCondition;
import org.enoch.snark.instance.model.to.PlanetData;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.model.uc.ResourceUC;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static java.util.Collections.singletonList;
import static org.enoch.snark.instance.model.technology.Ship.transporterLarge;
import static org.enoch.snark.instance.model.uc.ResourceUC.isNothingOrNull;


@RequiredArgsConstructor
@Component
@Scope("prototype")
public class FleetService {

    private final ShipService shipService;

    public FleetSendCommand transportFleet(ColonyEntity from, ColonyEntity to, Resources resources, Resources leave) {
        if (isNothingOrNull(resources)) return null;
        Resources needed = ResourceUC.toTransport(from, resources, leave);
        if (isNothingOrNull(needed)) return null;
//        Resources resourcesCondition = sum(needed, leave);

        ShipsMap transportShipsMap = new ShipsMap();
        transportShipsMap.put(transporterLarge, shipService.calculateShipCountForTransport(transporterLarge, resources));

        FleetSendCommand command = new FleetSendCommand();
        command.setSource(from);
        command.setTarget(new PlanetData(to));
        command.setMission(Mission.TRANSPORT);
        command.setShipsMap(transportShipsMap);
        command.addConditions(singletonList(new ResourceCondition(from.toPlanet(), resources, leave)));
        command.setLeaveResources(leave);
        command.setResources(needed);
        command.generateHash(null, needed.toString());
        return command;
    }
}
