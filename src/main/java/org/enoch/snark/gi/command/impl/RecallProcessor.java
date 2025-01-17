package org.enoch.snark.gi.command.impl;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.gi.EventContentGIR;
import org.enoch.snark.gi.types.GIUrl;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.si.QueueRunType;
import org.springframework.stereotype.Service;
import static org.enoch.snark.gi.types.UrlComponent.FLEETDISPATCH;

@Service
@RequiredArgsConstructor
public class RecallProcessor{

    public boolean execute(RecallCommand command) {
        GIUrl.openComponent(FLEETDISPATCH, null);
        boolean recall = new EventContentGIR().recall(command.promise);
        new UpdateFleetEventsCommand().execute();
        return recall;
    }

    @Override
    public String toString() {
        return  RecallProcessor.class.getSimpleName() + " " + "promise to sprint" ;
    }
}
