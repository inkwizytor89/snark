package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.exception.ShipDoNotExists;

import java.util.HashMap;
import java.util.Map;

import static org.enoch.snark.gi.macro.ShipEnum.*;

public class ExpeditionFleetCommand extends SendFleetCommand {

    private ColonyEntity colony;

//    public ExpeditionFleetCommand(ColonyEntity colony) {
//        super(FleetEntity.createExpedition(colony));
//        this.colony = colony;
//    }

    public ExpeditionFleetCommand(FleetEntity fleet) {
        super(fleet);
        this.colony = fleet.source;
    }

    @Override
    public boolean prepere() {
//        Planet expeditionDest = colony.toPlanet();
//        expeditionDest.position = 16;
        giUrlBuilder.openFleetView(fleet.source, fleet.getDestination(), Mission.EXPEDITION);
        autoComplete = true;
        if(!canSendExpedition()) {
            System.err.println("Colony "+colony+" have not enough ships - skipping expedition");
            return false;
        }
//        fleet = FleetEntity.createExpedition(colony);
        return true;
    }
//
//    @Override
//    public Map<ShipEnum, Long> buildShipsMap() {
//        ColonyEntity colony = fleet.source;
//        Map<ShipEnum, Long> shipsMap = new HashMap<>();
//        Long minSize = instance.calculateMinExpeditionSize();
//        if (minSize > 0) {
//            Long dt = colony.transporterLarge;
//            long possibleCount = dt / minSize;
//
//            if (possibleCount == 0) {
//                throw new ShipDoNotExists();
//            }
//            long toSend = dt / possibleCount;
//
//            shipsMap.put(transporterLarge, Math.min(Instance.getInstance().calculateMaxExpeditionSize(), toSend));
//            fleet.transporterLarge = toSend;
//            shipsMap.put(explorer, 1L);
//            fleet.explorer = 1L;
//        } else {
//            minSize = -minSize;
//            Long lt = colony.transporterSmall;
//            long possibleCount = lt / minSize;
//
//            if (possibleCount == 0) {
//                throw new ShipDoNotExists();
//            }
//            long toSend = lt / possibleCount;
//
//            shipsMap.put(transporterSmall, Math.min(-Instance.getInstance().calculateMaxExpeditionSize(), toSend));
//            fleet.transporterLarge = toSend;
//        }
//        return shipsMap;
//    }

    @Override
    public boolean execute() {
        return super.execute();
    }

    private boolean canSendExpedition() {
        return colony.transporterLarge >= fleet.transporterLarge &&
                colony.transporterSmall >= fleet.transporterSmall &&
                colony.explorer >= fleet.explorer;
//        Long expeditionShipsCount = Instance.getInstance().calculateMinExpeditionSize();
//        return colony.explorer > 0 && (
//                (expeditionShipsCount > 0 && colony.transporterLarge >= expeditionShipsCount) ||
//                (expeditionShipsCount <= 0 && colony.transporterSmall >= -expeditionShipsCount )
//                );
    }
}
