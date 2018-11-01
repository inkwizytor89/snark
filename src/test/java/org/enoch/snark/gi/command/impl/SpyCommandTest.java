package org.enoch.snark.gi.command.impl;

import org.enoch.snark.gi.AbstractSeleniumTest;
import org.junit.Test;

public class SpyCommandTest extends AbstractSeleniumTest {

    @Test
    public void execute() {
        final SpyCommand command = new SpyCommand(instance, sampleTarget);

        command.execute();
    }
}