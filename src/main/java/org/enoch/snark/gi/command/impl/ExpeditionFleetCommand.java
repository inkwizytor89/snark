package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.macro.Mission;

public class ExpeditionFleetCommand extends SendFleetCommand {

    public ExpeditionFleetCommand(FleetEntity fleet) {
        super(fleet);
    }

    @Override
    public boolean prepere() {
        giUrlBuilder.openFleetView(fleet.source, fleet.getDestination(), Mission.EXPEDITION);
        autoComplete = true;
        if(!canSendExpedition()) {
            System.err.println("Colony "+fleet.source+" have not enough ships - skipping expedition");
            return false;
        }
        return true;
    }

    @Override
    public boolean execute() {
        return super.execute();
    }

    private boolean canSendExpedition() {
        return fleet.source.transporterLarge >= fleet.transporterLarge &&
                fleet.source.transporterSmall >= fleet.transporterSmall &&
                fleet.source.explorer >= fleet.explorer;
    }
}
