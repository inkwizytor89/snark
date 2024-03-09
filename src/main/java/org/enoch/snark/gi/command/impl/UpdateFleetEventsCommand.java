package org.enoch.snark.gi.command.impl;

import org.enoch.snark.gi.GeneralGIR;
import org.enoch.snark.gi.macro.GIUrl;
import org.enoch.snark.instance.commander.Navigator;

import static org.enoch.snark.gi.macro.UrlComponent.FLEETDISPATCH;

public class UpdateFleetEventsCommand extends AbstractCommand {

    public UpdateFleetEventsCommand() {
        super();
    }

    @Override
    public boolean execute() {
        GIUrl.openComponent(FLEETDISPATCH, null);
        Navigator.getInstance().informAboutEventFleets(new GeneralGIR().readEventFleet());
        return true;
    }

    @Override
    public String toString() {
        return "UpdateFleetEventsCommand";
    }
}
