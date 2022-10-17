package org.enoch.snark.common;

import org.enoch.snark.gi.command.impl.AbstractCommand;
import org.enoch.snark.instance.Instance;

public class WaitingThread extends Thread {

    public WaitingThread(AbstractCommand command, int secondsToDelay) {
        super(() -> {
            SleepUtil.secondsToSleep(secondsToDelay);
            Instance.getInstance().push(command);
        });
    }

    @Override
    public void start() {
        super.start();
    }
}
