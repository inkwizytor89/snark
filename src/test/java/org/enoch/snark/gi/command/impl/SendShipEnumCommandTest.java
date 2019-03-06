package org.enoch.snark.gi.command.impl;

import org.enoch.snark.gi.AbstractSeleniumTest;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.model.Fleet;
import org.junit.Test;

public class SendShipEnumCommandTest extends AbstractSeleniumTest {

    @Test
    public void execute() {
        final Fleet fleet = new Fleet().put(ShipEnum.SON, 2);
        final SendFleetCommandOld command = new SendFleetCommandOld(instance, sampleTarget, Mission.SPY, fleet);

        command.execute();
    }
}