package org.enoch.snark.gi.command;

import org.enoch.snark.common.WaitingThread;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.Utils;

import java.util.concurrent.TimeUnit;

public abstract class AbstractCommand {
    private AbstractCommand afterCommand;
    private Long secondsToDelay;
    protected Instance instance;
    private CommandType type;
    public int failed = 0;

    protected AbstractCommand(Instance instance, CommandType type) {
        this.instance = instance;
        this.type = type;
    }

    public abstract boolean execute();

    public void doAfter() {
        if(afterCommand == null) {
            return;
        }
        Runnable task = () -> {
            try {
                if (secondsToDelay > 0) {
                    TimeUnit.SECONDS.sleep(secondsToDelay);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            instance.commander.push(afterCommand);
        };

        new WaitingThread(task, secondsToDelay).start();
    }

    public void retry(Long secondsToDelay) {
        Runnable task = () -> {
            instance.commander.push(this);
        };

        new WaitingThread(task, secondsToDelay).start();
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
