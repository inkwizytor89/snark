package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.exception.ShipDoNotExists;

import java.util.HashMap;
import java.util.Map;

import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_BASE_FLEET;
import static org.enoch.snark.gi.macro.ShipEnum.*;

public class ExpeditionFleetCommand extends SendFleetCommand {

    private ColonyEntity colony;

    public ExpeditionFleetCommand(ColonyEntity colony) {
        super(FleetEntity.createExpeditionFleet(colony));
        this.colony = colony;
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

            shipsMap.put(transporterLarge, Math.min(Instance.getInstance().calculateMaxExpeditionSize(), toSend));
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

            shipsMap.put(transporterSmall, Math.min(-Instance.getInstance().calculateMaxExpeditionSize(), toSend));
            fleet.dt = toSend;
        }
        return shipsMap;
    }

    @Override
    public boolean openFleetWindow() {
        giUrlBuilder.open(PAGE_BASE_FLEET, colony, true);
        if(!canSendExpedition()) {
            return true;
        }
        fleet = FleetEntity.createExpeditionFleet(colony);
        return false;
    }

    @Override
    public boolean execute() {
        return super.execute();
    }

    private boolean canSendExpedition() {
//        return true;
        Long expeditionShipsCount = Instance.getInstance().calculateMinExpeditionSize();
        return colony.explorer > 0 && (
                (expeditionShipsCount > 0 && colony.transporterLarge >= expeditionShipsCount) ||
                (expeditionShipsCount <= 0 && colony.transporterSmall >= -expeditionShipsCount )
                );
    }
}
