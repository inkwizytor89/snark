package org.enoch.snark.action.command;

import org.enoch.snark.instance.model.to.FleetPlan;
import org.enoch.snark.instance.si.QueueRunType;
import org.enoch.snark.instance.model.to.FleetPromise;

public class RecallCommand extends AbstractCommand {

    public FleetPromise promise;
    public FleetPlan plan;

    public RecallCommand(FleetPromise promise) {
        super();
        this.promise = promise;
        setRunType(QueueRunType.MAJOR);
    }

    public RecallCommand(FleetPlan plan) {
        super();
        this.plan = plan;
        setRunType(QueueRunType.MAJOR);
    }

    @Override
    public String toString() {
        return  RecallCommand.class.getSimpleName() + " " + promise ;
    }
}
