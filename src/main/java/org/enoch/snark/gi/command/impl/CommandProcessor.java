package org.enoch.snark.gi.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandProcessor {

    private OpenPageProcessor openPageProcessor;
    private BuildProcessor buildProcessor;

    public void execute(AbstractCommand command) {
        if (command instanceof OpenPageCommand) openPageProcessor.execute((OpenPageCommand) command);
        else if (command instanceof BuildCommand) buildProcessor.execute((BuildCommand) command);
    }
}
