package org.enoch.snark.gi.command.impl;

import org.enoch.snark.gi.AbstractSeleniumTest;
import org.enoch.snark.gi.command.SpyObserver;
import org.enoch.snark.model.SpyInfo;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReadMessageCommandTest extends AbstractSeleniumTest implements SpyObserver {

    @Test
    public void execute() {
        final ReadMessageCommand command = new ReadMessageCommand(instance, sampleTarget, this);

        command.execute();
    }

    @Override
    public void report(SpyInfo info) {
        assertTrue(info != null);
    }
}