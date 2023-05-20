package org.enoch.snark.common;

import org.enoch.snark.gi.command.impl.AbstractCommand;
import org.enoch.snark.instance.Commander;

public class WaitingThread extends Thread {

    private final AbstractCommand command;
    private final boolean shouldUseFleetActionQueue;
    private final int secondsToDelay;

    public WaitingThread(AbstractCommand command, boolean shouldUseFleetActionQueue, int secondsToDelay) {
        super();
        this.command = command;
        this.shouldUseFleetActionQueue = shouldUseFleetActionQueue;
        this.secondsToDelay = secondsToDelay;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void run() {
        super.run();
        SleepUtil.secondsToSleep(secondsToDelay);
        Commander commander = Commander.getInstance();
        if(shouldUseFleetActionQueue) {
            commander.pushFleet(command);
        } else {
            commander.push(command);
        }
    }
}
