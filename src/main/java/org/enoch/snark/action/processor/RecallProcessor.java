package org.enoch.snark.action.processor;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.RecallCommand;
import org.enoch.snark.action.command.UpdateFleetEventsCommand;
import org.enoch.snark.instance.si.module.consumer.gi.EventContentGIR;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.enoch.snark.instance.si.module.consumer.gi.types.GIUrl;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;

@Component
@Scope("prototype")
public class RecallProcessor{

    public boolean execute(GI gi, RecallCommand command) {
        gi.url().openComponent(FLEETDISPATCH, null);
        boolean recall = new EventContentGIR(gi).recall(command.promise);
        new UpdateFleetEventsProcessor().execute(gi, null);
        return recall;
    }

    @Override
    public String toString() {
        return  RecallProcessor.class.getSimpleName() + " " + "promise to sprint" ;
    }
}
