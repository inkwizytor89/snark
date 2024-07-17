package org.enoch.snark.gi.command.impl;

import org.enoch.snark.gi.GeneralGIR;
import org.enoch.snark.gi.types.GIUrl;
import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.service.Navigator;

import java.util.List;

import static org.enoch.snark.gi.types.UrlComponent.FLEETDISPATCH;

public class UpdateFleetEventsCommand extends AbstractCommand {

    public UpdateFleetEventsCommand() {
        super();
    }

    @Override
    public boolean execute() {
        GIUrl.openComponent(FLEETDISPATCH, null);
        List<EventFleet> eventFleetList = new GeneralGIR().readEventFleet();
        Navigator.getInstance().informAboutEventFleets(eventFleetList);
        return true;
    }

    @Override
    public String toString() {
        return "UpdateFleetEventsCommand";
    }
}
