package org.enoch.snark.gi.command;

import org.enoch.snark.gi.command.impl.PauseCommand;
import org.enoch.snark.instance.Instance;

import java.util.logging.Logger;

public abstract class AbstractCommand {
    protected static final Logger log = Logger.getLogger( AbstractCommand.class.getName() );

    private AbstractCommand afterCommand;
    private int secoundToDelay;
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
        instance.commander.push(new PauseCommand(instance, afterCommand, secoundToDelay));
    }

    protected void setSecoundToDelayAfterCommand(int secoundToDelay) {
        this.secoundToDelay = secoundToDelay;
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
}
