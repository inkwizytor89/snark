package org.enoch.snark.gi.command.impl;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.gi.types.GIUrl;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenPageProcessor {

    public boolean execute(OpenPageCommand command) {
//        System.err.println(hash()+": Open "+component+" on "+ colony);
            GIUrl.openComponent(command.component, command.colony);
        return true;
    }
}
