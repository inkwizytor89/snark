package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.model.exception.ShipDoNotExists;

import java.util.HashMap;
import java.util.Map;

import static org.enoch.snark.gi.macro.ShipEnum.explorer;
import static org.enoch.snark.gi.macro.ShipEnum.transporterLarge;

public class ExpeditionFleetCommand extends SendFleetCommand {

    public ExpeditionFleetCommand(FleetEntity fleet) {
        super(fleet);
    }

    @Override
    public Map<ShipEnum, Long> buildShipsMap() {
        ColonyEntity colony = fleet.source;
        Map<ShipEnum, Long> shipsMap = new HashMap<>();
        Long minSize = instance.calcutateMinExpeditionSize();
        Long dt = colony.transporterLarge;
        long possibleCount = dt / minSize;

        System.err.println("dt is "+dt);
        System.err.println("possibleCount is "+possibleCount);
        if(possibleCount == 0) {
            throw new ShipDoNotExists();
        }
        long toSend = dt / possibleCount;

        System.err.println("to send "+toSend);

        shipsMap.put(transporterLarge, toSend);
        fleet.dt = toSend;
        shipsMap.put(explorer, 1L);
        fleet.pf = 1L;
        return shipsMap;
    }

}
