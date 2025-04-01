package org.enoch.snark.action.processor;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.OpenPageCommand;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.enoch.snark.instance.si.module.consumer.gi.types.GIUrl;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Scope("prototype")
public class OpenPageProcessor {

    public boolean execute(GI gi, OpenPageCommand command) {
//        System.err.println(hash()+": Open "+component+" on "+ colony);
        gi.url().openComponent(command.component, command.colony);
        return true;
    }
}
