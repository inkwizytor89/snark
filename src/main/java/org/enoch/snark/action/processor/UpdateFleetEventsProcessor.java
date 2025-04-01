package org.enoch.snark.action.processor;

import org.enoch.snark.action.command.RecallCommand;
import org.enoch.snark.action.command.UpdateFleetEventsCommand;
import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.si.module.consumer.gi.EventContentGIR;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;

@Component
@Scope("prototype")
public class UpdateFleetEventsProcessor {

//    public UpdateFleetEventsProcessor() {
//        super();
//    }

    public boolean execute(GI gi, UpdateFleetEventsCommand command) {
        gi.url().openComponent(FLEETDISPATCH, null);
        List<EventFleet> eventFleetList = new EventContentGIR(gi).readEventFleet();
        Navigator.getInstance().informAboutEventFleets(eventFleetList);
        return true;
    }

    @Override
    public String toString() {
        return "UpdateFleetEventsCommand";
    }
}
