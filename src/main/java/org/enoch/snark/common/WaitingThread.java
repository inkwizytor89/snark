package org.enoch.snark.common;

import org.enoch.snark.action.command.FollowingAction;
import org.enoch.snark.instance.si.CommandDeque;

public class WaitingThread extends Thread {

    private final FollowingAction followingAction;
    private final CommandDeque commandDeque;

    public WaitingThread(FollowingAction followingAction, CommandDeque commandDeque) {
        super();
        this.followingAction = followingAction;
        this.commandDeque = commandDeque;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void run() {
        super.run();
        SleepUtil.secondsToSleep(followingAction.getSecondsToDelay());
        commandDeque.push(followingAction.getCommand());
    }
}
