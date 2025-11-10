package org.enoch.snark.action.processor;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.action.command.*;
import org.enoch.snark.action.command.status.CommandStatus;
import org.enoch.snark.action.command.status.ExecutionIssue;
import org.enoch.snark.action.command.status.ExecutionStatus;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.enoch.snark.action.command.status.ExecutionStatus.IN_PROGRESS;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class CommandProcessor {

    private final BuildProcessor buildProcessor;
    private final OpenPageProcessor openPageProcessor;
    private final GalaxyAnalyzeProcessor galaxyAnalyzeProcessor;
    private final LoadColoniesProcessor loadColoniesProcessor;
    private final ReadMessageProcessor readMessageProcessor;
    private final RecallProcessor recallProcessor;
    private final SendMessageToPlayerProcessor sendMessageToPlayerProcessor;
    private final UpdateFleetEventsProcessor updateFleetEventsProcessor;
    private final UpdateResearchProcessor updateResearchProcessor;
    private final FleetSendProcessor fleetSendProcessor;

    @Transactional
    public ExecutionIssue execute(GI gi, AbstractCommand abstractCommand) {
        CommandStatus status = abstractCommand.getStatus();
        status.setStatus(IN_PROGRESS);
        return switch (abstractCommand) {
            case OpenPageCommand open -> openPageProcessor.execute(gi, open);
            case BuildCommand build -> buildProcessor.execute(gi, build);
            case GalaxyAnalyzeCommand galaxyAnalyze -> galaxyAnalyzeProcessor.execute(gi, galaxyAnalyze);
            case LoadColoniesCommand loadColonies -> loadColoniesProcessor.execute(gi, loadColonies);
            case ReadMessageCommand readMessage -> readMessageProcessor.execute(gi);
            case RecallCommand readMessage -> recallProcessor.execute(gi, readMessage);
            case SendMessageToPlayerCommand command -> sendMessageToPlayerProcessor.execute(gi, command);
            case UpdateFleetEventsCommand command -> updateFleetEventsProcessor.execute(gi, command);
            case UpdateResearchCommand command -> updateResearchProcessor.execute(gi, command);
            case SendCommand command -> fleetSendProcessor.execute(gi, command);
            default -> throw new NotImplementedException("Missing processor for "+abstractCommand.getClass().getSimpleName());
        };

//        status.setStatus();
    }
}
