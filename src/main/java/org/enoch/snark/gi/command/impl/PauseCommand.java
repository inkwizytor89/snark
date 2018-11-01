package org.enoch.snark.gi.command.impl;

import org.enoch.snark.gi.command.AbstractCommand;
import org.enoch.snark.gi.command.CommandType;
import org.enoch.snark.instance.Instance;

import java.util.concurrent.TimeUnit;

public class PauseCommand extends AbstractCommand {

    private final AbstractCommand command;
    private final TimeUnit timeUnit;
    private final long value;

    public PauseCommand(Instance instance, AbstractCommand command, long numberOfSecounds) {
        this(instance,command, TimeUnit.SECONDS, numberOfSecounds);

    }

    public PauseCommand(Instance instance, AbstractCommand command, TimeUnit timeUnit, long value) {
        super(instance, CommandType.CALCULATION);
        this.command = command;
        this.timeUnit = timeUnit;
        this.value = value;
    }

    @Override
    public boolean execute() {
        Runnable task = () -> {
                try {
                    timeUnit.sleep(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                instance.commander.push(command);
        };

        new Thread(task).start();
        return true;
    }

    @Override
    public String toString() {
        return "Sleep "+value+" "+timeUnit+" for: "+command;
    }
}
