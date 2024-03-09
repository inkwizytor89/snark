package org.enoch.snark.gi.command.impl;

import org.enoch.snark.gi.macro.GIUrl;

public class UpdateResearchCommand extends AbstractCommand {

    private boolean updateCurrentState = false;

    public UpdateResearchCommand() {
        super();
    }

    @Override
    public boolean execute() {
        // load only research for player and put logic from GIUrlBuilder to here
        GIUrl.openResearch();
        return true;
    }

    @Override
    public String toString() {
        return "UpdateResearchCommand";
    }
}
