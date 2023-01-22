package org.enoch.snark.gi.command.impl;

import org.enoch.snark.common.WaitingThread;
import org.enoch.snark.instance.Instance;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCommand {
    public AbstractCommand afterCommand;
    public Integer secondsToDelay = 0;
    protected Instance instance;
    private final CommandType type;
    public int failed = 0;
    Integer priority = 100;
    private List<String> tags = new ArrayList<>();

    protected AbstractCommand(Instance instance, CommandType type) {
        this.instance = instance;
        this.type = type;
    }

    protected AbstractCommand(CommandType type) {
        this.instance = Instance.getInstance();
        this.type = type;
    }

    public abstract boolean execute();

    public void doAfter() {
        if(afterCommand == null) {
            return;
        }
//        System.err.println(afterCommand + " with delay " + secondsToDelay);
        new WaitingThread(afterCommand, secondsToDelay).start();
    }

    public boolean isAfterCommand() {
        return afterCommand != null;
    }

    public void retry(Integer secondsToDelay) {
        new WaitingThread(this, secondsToDelay).start();
    }

    protected void setSecondToDelayAfterCommand(Integer secoundToDelay) {
        this.secondsToDelay = secoundToDelay;
    }

    public AbstractCommand getAfterCommand() {
        return afterCommand;
    }

    public void setAfterCommand(AbstractCommand afterCommand) {
        this.afterCommand = afterCommand;
    }

    public CommandType getType() {
        return type;
    }

    public boolean requiredGI() {
        return type == CommandType.PRIORITY_REQUIERED || type ==CommandType.NORMAL_REQUIERED;
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
