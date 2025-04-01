package org.enoch.snark.action.command;

import org.enoch.snark.instance.si.module.consumer.gi.types.GIUrl;

public class UpdateResearchCommand extends AbstractCommand {

    public UpdateResearchCommand() {
        super();
    }

//    @Override
//    public boolean execute() {
//        GIUrl.openResearch();
//        return true;
//    }

    @Override
    public String toString() {
        return "UpdateResearchCommand";
    }
}
