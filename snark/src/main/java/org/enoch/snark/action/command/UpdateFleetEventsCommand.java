package org.enoch.snark.action.command;

import org.enoch.snark.instance.si.module.consumer.gi.EventContentGIR;
import org.enoch.snark.instance.si.module.consumer.gi.types.GIUrl;
import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.service.Navigator;

import java.util.List;

import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;

public class UpdateFleetEventsCommand extends AbstractCommand {

    public UpdateFleetEventsCommand() {
        super();
    }

//    @Override
//    public boolean execute() {
//        GIUrl.openComponent(FLEETDISPATCH, null);
//        List<EventFleet> eventFleetList = new EventContentGIR().readEventFleet();
//        Navigator.getInstance().informAboutEventFleets(eventFleetList);
//        return true;
//    }

    @Override
    public String toString() {
        return "UpdateFleetEventsCommand";
    }
}
