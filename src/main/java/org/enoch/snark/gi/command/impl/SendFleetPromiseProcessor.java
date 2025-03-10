package org.enoch.snark.gi.command.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.gi.SendFleetGIR;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendFleetPromiseProcessor {

    public static final Long TIME_BUFFER = 3L;

    private final SendFleetGIR gir = new SendFleetGIR();

    public boolean execute(SendFleetPromiseCommand command) {
        throw new NotImplementedException("To remove in spring version");

    }
}
