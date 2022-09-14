package org.enoch.snark.gi.command;

import org.enoch.snark.common.WaitingThread;
import org.enoch.snark.instance.Instance;

public abstract class AbstractCommand {
    public AbstractCommand afterCommand;
    public Long secondsToDelay = 0L;
    protected Instance instance;
    private CommandType type;
    public int failed = 0;

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
        System.err.println(afterCommand + " with delay " + secondsToDelay);
        new WaitingThread(afterCommand, secondsToDelay).start();
    }

    public boolean isAfterCommand() {
        return afterCommand != null;
    }

    public void retry(Long secondsToDelay) {
        new WaitingThread(this, secondsToDelay).start();
    }

    protected void setSecoundToDelayAfterCommand(Long secoundToDelay) {
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
        return type == CommandType.FLEET_REQUIERED || type ==CommandType.INTERFACE_REQUIERED;
    }

    public void onInterrupt() {
    }
}
