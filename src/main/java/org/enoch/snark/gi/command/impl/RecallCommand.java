package org.enoch.snark.gi.command.impl;

import org.enoch.snark.gi.EventContentGIR;
import org.enoch.snark.gi.types.GIUrl;
import org.enoch.snark.instance.commander.QueueRunType;
import org.enoch.snark.instance.model.to.FleetPromise;

import static org.enoch.snark.gi.types.UrlComponent.FLEETDISPATCH;

public class RecallCommand extends AbstractCommand {

    private final FleetPromise promise;

    public RecallCommand(FleetPromise promise) {
        super();
        this.promise = promise;
        setRunType(QueueRunType.MAJOR);
    }

    @Override
    public boolean execute() {
        GIUrl.openComponent(FLEETDISPATCH, null);
        return new EventContentGIR().recall(promise);
    }

    @Override
    public String toString() {
        return  RecallCommand.class.getSimpleName() + " " + promise ;
    }
}
