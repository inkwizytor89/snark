package org.enoch.snark.action.command;

import org.enoch.snark.instance.model.to.FleetPlan;
import org.enoch.snark.instance.si.QueueRunType;

public class RecallCommand extends AbstractCommand {

    public FleetPlan plan;

    public RecallCommand(FleetPlan plan) {
        super();
        this.plan = plan;
        setRunType(QueueRunType.MAJOR);
    }

    @Override
    public String toString() {
        return  RecallCommand.class.getSimpleName() + " " + plan ;
    }
}
