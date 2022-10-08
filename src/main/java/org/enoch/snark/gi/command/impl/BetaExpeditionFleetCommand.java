package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.model.exception.ShipDoNotExists;

import java.util.HashMap;
import java.util.Map;

import static org.enoch.snark.gi.macro.ShipEnum.*;

public class BetaExpeditionFleetCommand extends SendFleetCommand {

    public BetaExpeditionFleetCommand(FleetEntity fleet) {
        super(fleet);
    }

    @Override
    public Map<ShipEnum, Long> buildShipsMap() {
        ColonyEntity colony = fleet.source;
        Map<ShipEnum, Long> shipsMap = new HashMap<>();
        Long minSize = instance.calculateMinExpeditionSize();
        if (minSize > 0) {
            Long dt = colony.transporterLarge;
            long possibleCount = dt / minSize;

            if (possibleCount == 0) {
                throw new ShipDoNotExists();
            }
            long toSend = dt / possibleCount;

            shipsMap.put(transporterLarge, toSend);
            fleet.dt = toSend;
            shipsMap.put(explorer, 1L);
            fleet.pf = 1L;
        } else {
            minSize = -minSize;
            Long lt = colony.transporterSmall;
            long possibleCount = lt / minSize;

            if (possibleCount == 0) {
                throw new ShipDoNotExists();
            }
            long toSend = lt / possibleCount;

            shipsMap.put(transporterSmall, toSend);
            fleet.dt = toSend;
        }
        return shipsMap;
    }

}
