package org.enoch.snark.action.command;

import org.enoch.snark.instance.si.QueueRunType;
import org.enoch.snark.instance.model.to.FleetPromise;

public class RecallCommand extends AbstractCommand {

    public final FleetPromise promise;

    public RecallCommand(FleetPromise promise) {
        super();
        this.promise = promise;
        setRunType(QueueRunType.MAJOR);
    }

    @Override
    public String toString() {
        return  RecallCommand.class.getSimpleName() + " " + promise ;
    }
}
