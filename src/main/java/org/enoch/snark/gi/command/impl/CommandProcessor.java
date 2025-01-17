package org.enoch.snark.gi.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandProcessor {

    private OpenPageProcessor openPageProcessor;
    private BuildProcessor buildProcessor;
    private GalaxyAnalyzeProcessor galaxyAnalyzeProcessor;
    private LoadColoniesProcessor loadColoniesProcessor;
    private ReadMessageProcessor readMessageProcessor;
    private RecallProcessor recallProcessor;

    public void execute(AbstractCommand command) {
        if (command instanceof OpenPageCommand) openPageProcessor.execute((OpenPageCommand) command);
        else if (command instanceof BuildCommand) buildProcessor.execute((BuildCommand) command);
        else if (command instanceof GalaxyAnalyzeCommand) galaxyAnalyzeProcessor.execute((GalaxyAnalyzeCommand) command);
        else if (command instanceof LoadColoniesCommand) loadColoniesProcessor.execute((LoadColoniesCommand) command);
        else if (command instanceof ReadMessageCommand) readMessageProcessor.execute();
        else if (command instanceof RecallCommand) recallProcessor.execute((RecallCommand)command);
    }
}
