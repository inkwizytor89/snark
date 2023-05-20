package org.enoch.snark.gi.command.impl;

import org.enoch.snark.common.WaitingThread;
import org.enoch.snark.instance.Instance;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCommand {
    private AbstractCommand afterCommand;
    private boolean shouldUseFleetActionQueue;
    public int secondsToDelay = 0;
    protected Instance instance;
    public int failed = 0;
    Integer priority = 100;
    private final List<String> tags = new ArrayList<>();


    protected AbstractCommand() {
        this.instance = Instance.getInstance();
    }

    public abstract boolean execute();

    public void doAfter() {
        if(afterCommand == null) {
            return;
        }

        WaitingThread waitingThread = new WaitingThread(afterCommand, shouldUseFleetActionQueue, secondsToDelay);
        waitingThread.start();
    }

    public boolean isAfterCommand() {
        return afterCommand != null;
    }

    public void retry(Integer secondsToDelay) {
        new WaitingThread(this, shouldUseFleetActionQueue, secondsToDelay).start();
    }

    protected void setSecondToDelayAfterCommand(Integer secondToDelay) {
        this.secondsToDelay = secondToDelay;
    }

    public void setAfterCommand(AbstractCommand afterCommand) {
        setAfterCommand(afterCommand, false);
    }
    public void setAfterCommand(AbstractCommand afterCommand, boolean shouldUseFleetActionQueue) {
        this.afterCommand = afterCommand;
        this.shouldUseFleetActionQueue = shouldUseFleetActionQueue;
    }

    public void onInterrupt() {
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public List<String> getTags() {
        return tags;
    }
}
