package org.enoch.snark.action.processor;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.RecallCommand;
import org.enoch.snark.action.command.status.ExecutionIssue;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.si.module.consumer.gi.EventContentGIR;
import org.enoch.snark.instance.si.module.consumer.gi.Wd;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class RecallProcessor{

    private final UpdateFleetEventsProcessor updateFleetEventsProcessor;
    private final ColonyRepository colonyRepository;

    public ExecutionIssue execute(Wd wd, RecallCommand command) {
        ColonyEntity colony = wd.url().openComponent(FLEETDISPATCH, null);
        colonyRepository.save(colony);

        boolean recall = new EventContentGIR(wd).recall(command.plan);
        updateFleetEventsProcessor.execute(wd, null);
        return recall ? ExecutionIssue.NO_ISSUE : ExecutionIssue.OTHER;
    }

    @Override
    public String toString() {
        return  RecallProcessor.class.getSimpleName() + " " + "promise to sprint" ;
    }
}
