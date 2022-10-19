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

    public ExpeditionFleetCommand(FleetEntity fleet) {
        super(fleet);
        this.colony = fleet.source;
    }

    @Override
    public boolean prepere() {
        giUrlBuilder.openFleetView(fleet.source, fleet.getDestination(), Mission.EXPEDITION);
        autoComplete = true;
        if(!canSendExpedition()) {
            System.err.println("Colony "+colony+" have not enough ships - skipping expedition");
            return false;
        }
        return true;
    }

    @Override
    public boolean execute() {
        return super.execute();
    }

    private boolean canSendExpedition() {
        return colony.transporterLarge >= fleet.transporterLarge &&
                colony.transporterSmall >= fleet.transporterSmall &&
                colony.explorer >= fleet.explorer;
    }
}
