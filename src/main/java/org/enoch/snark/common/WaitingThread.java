package org.enoch.snark.common;

import org.enoch.snark.instance.Utils;

public class WaitingThread extends Thread {

    private final long secondsToDelay;

    public WaitingThread(Runnable task, long secondsToDelay) {
        super(task);
        this.secondsToDelay = secondsToDelay;
    }

    @Override
    public void start() {
        Utils.secondsToSleep(secondsToDelay);
        super.start();
    }
}
