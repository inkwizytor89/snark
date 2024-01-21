package org.enoch.snark.common;

import org.enoch.snark.gi.command.impl.FollowingAction;

public class WaitingThread extends Thread {

    private FollowingAction followingAction;

    public WaitingThread(FollowingAction followingAction) {
        super();
        this.followingAction = followingAction;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void run() {
        super.run();
        SleepUtil.secondsToSleep(followingAction.getSecondsToDelay());
        followingAction.getCommand().push();
    }
}
