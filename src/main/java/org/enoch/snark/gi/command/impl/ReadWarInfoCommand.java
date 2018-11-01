package org.enoch.snark.gi.command.impl;

import org.enoch.snark.gi.command.AbstractCommand;
import org.enoch.snark.gi.command.AttackObserver;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.Planet;

import static org.enoch.snark.gi.command.CommandType.INTERFACE_REQUIERED;

public class ReadWarInfoCommand extends AbstractCommand {
    private final Planet planet;
    private final AttackObserver observer;

    public ReadWarInfoCommand(Instance instance, Planet planet, AttackObserver observer) {
        super(instance, INTERFACE_REQUIERED);
        this.planet = planet;
        this.observer = observer;
    }

    @Override
    public boolean execute() {
        return true;
    }

    @Override
    public String toString() {
        return "load war message from "+planet+ "[not implemented yet]";
    }
}
