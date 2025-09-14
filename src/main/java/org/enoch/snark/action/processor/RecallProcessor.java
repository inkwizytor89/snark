package org.enoch.snark.action.processor;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.RecallCommand;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.si.module.consumer.gi.EventContentGIR;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class RecallProcessor{

    private final UpdateFleetEventsProcessor updateFleetEventsProcessor;
    private final ColonyRepository colonyRepository;

    public boolean execute(GI gi, RecallCommand command) {
        ColonyEntity colony = gi.url().openComponent(FLEETDISPATCH, null);
        colonyRepository.save(colony);

        boolean recall = new EventContentGIR(gi).recall(command.plan);
        updateFleetEventsProcessor.execute(gi, null);
        return recall;
    }

    @Override
    public String toString() {
        return  RecallProcessor.class.getSimpleName() + " " + "promise to sprint" ;
    }
}
