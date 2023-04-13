package org.enoch.snark.common;

import org.enoch.snark.gi.command.impl.AbstractCommand;
import org.enoch.snark.instance.Commander;

public class WaitingThread extends Thread {

    public WaitingThread(AbstractCommand command, boolean shouldUseFleetActionQueue, int secondsToDelay) {
        super(() -> {
            SleepUtil.secondsToSleep(secondsToDelay);
            Commander commander = Commander.getInstance();
            if(shouldUseFleetActionQueue) {
                commander.pushFleet(command);
            } else {
                commander.push(command);
            }
        });
    }

    @Override
    public void start() {
        super.start();
    }
}
