package org.enoch.snark.gi.command.impl;

import org.enoch.snark.gi.types.GIUrl;

public class UpdateResearchCommand extends AbstractCommand {

    public UpdateResearchCommand() {
        super();
    }

    @Override
    public boolean execute() {
        GIUrl.openResearch();
        return true;
    }

    @Override
    public String toString() {
        return "UpdateResearchCommand";
    }
}
