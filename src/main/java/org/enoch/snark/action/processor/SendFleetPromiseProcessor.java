package org.enoch.snark.action.processor;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.action.command.SendFleetPromiseCommand;
import org.enoch.snark.instance.si.module.consumer.gi.SendFleetGIR;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Scope("prototype")
public class SendFleetPromiseProcessor {

    public static final Long TIME_BUFFER = 3L;

    private final SendFleetGIR gir = new SendFleetGIR(null);

    public boolean execute(SendFleetPromiseCommand command) {
        throw new NotImplementedException("To remove in spring version");

    }
}
