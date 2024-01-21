package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.macro.Mission;

public class ExpeditionFleetCommand extends SendFleetCommand {

    private final boolean battleExtension;

    public ExpeditionFleetCommand(FleetEntity fleet, boolean battleExtension) {
        super(fleet);
        this.battleExtension = battleExtension;
    }

    @Override
    public boolean prepere() {
        giUrlBuilder.openFleetView(fleet.source, fleet.getDestination(), Mission.EXPEDITION);
        autoComplete = true;
        if(!canSendExpedition()) {
            System.err.println("Colony "+fleet.source+" have not enough ships - skipping expedition");
            return false;
        }
        if(battleExtension) {
            addBattlerShip();
        }
        return true;
    }

    private void addBattlerShip() {
        ColonyEntity source = ColonyDAO.getInstance().fetch(fleet.source);
//        if(source.reaper > 0) {
//            fleet.reaper = 1L;
//        } else if (source.destroyer > 0) {
//            fleet.destroyer = 1L;
//        } else if (source.bomber > 0) {
//            fleet.bomber = 1L;
//        } else if (source.interceptor > 0) {
//            fleet.interceptor = 1L;
//        } else
        if (source.battleship > 0) {
            fleet.battleship = 1L;
        } else if (source.cruiser > 0 && source.explorer < 1) {
            fleet.cruiser = 1L;
        } else if (source.fighterHeavy > 0 && source.explorer < 1) {
                fleet.fighterHeavy = 1L;
            }
    }

    @Override
    public boolean execute() {
        return super.execute();
    }

    private boolean canSendExpedition() {
        if(fleet.source.transporterLarge == null) fleet.source.transporterLarge = 0L;
        if(fleet.source.transporterSmall == null) fleet.source.transporterSmall = 0L;
        if(fleet.source.explorer == null) fleet.source.explorer = 0L;

        return zeroForNull(fleet.source.transporterLarge) >= zeroForNull(fleet.transporterLarge) &&
                zeroForNull(fleet.source.transporterSmall) >= zeroForNull(fleet.transporterSmall) &&
                        zeroForNull(fleet.source.explorer) >= zeroForNull(fleet.explorer);
    }

    private Long zeroForNull(Long value) {
        if(value == null) return 0L;
        return value;
    }
}
