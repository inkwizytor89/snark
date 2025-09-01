package org.enoch.snark.action.processor;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.RecallCommand;
import org.enoch.snark.action.command.UpdateFleetEventsCommand;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.si.module.consumer.gi.EventContentGIR;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class UpdateFleetEventsProcessor {

    private final ColonyRepository colonyRepository;

//    public UpdateFleetEventsProcessor() {
//        super();
//    }

    public boolean execute(GI gi, UpdateFleetEventsCommand command) {
        ColonyEntity colony = gi.url().openComponent(FLEETDISPATCH, null);
        colonyRepository.save(colony);

        List<EventFleet> eventFleetList = new EventContentGIR(gi).readEventFleet();
        Navigator.getInstance().informAboutEventFleets(eventFleetList);
        return true;
    }

    @Override
    public String toString() {
        return "UpdateFleetEventsCommand";
    }
}
