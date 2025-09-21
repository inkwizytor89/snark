package org.enoch.snark.action.command;

import org.enoch.snark.instance.si.QueueRunType;

public class RecallCommand extends AbstractCommand {

    public SendCommand plan;

    public RecallCommand(SendCommand plan) {
        super();
        this.plan = plan;
        setRunType(QueueRunType.MAJOR);
    }

    @Override
    public String toString() {
        return  RecallCommand.class.getSimpleName() + " " + plan ;
    }
}
