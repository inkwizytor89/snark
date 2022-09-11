package org.enoch.snark.common;

import org.enoch.snark.gi.command.AbstractCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.Utils;

public class WaitingThread extends Thread {

    public WaitingThread(AbstractCommand command, long secondsToDelay) {
        super(() -> {
            Utils.secondsToSleep(secondsToDelay);
            Instance.getInstance().commander.push(command);
        });
    }

    @Override
    public void start() {
        super.start();
    }
}
