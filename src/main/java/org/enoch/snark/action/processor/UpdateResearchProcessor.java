package org.enoch.snark.action.processor;

import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.snark.action.command.RecallCommand;
import org.enoch.snark.action.command.UpdateResearchCommand;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.enoch.snark.instance.si.module.consumer.gi.types.GIUrl;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class UpdateResearchProcessor {

//    public UpdateResearchProcessor() {
//        super();
//    }

    public boolean execute(GI gi, UpdateResearchCommand command) {
        new GIUrl(gi).openResearch();
        return true;
    }

    @Override
    public String toString() {
        return "UpdateResearchCommand";
    }
}
