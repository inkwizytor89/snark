package org.enoch.snark.instance;

import org.enoch.snark.gi.command.AbstractCommand;

import java.util.List;

public interface Commander {

    void push(AbstractCommand command);

    List<AbstractCommand> peekFleetQueue();

    AbstractCommand getActualProcessedCommand();

    boolean isRunning();

    void setFleetStatus(int fleetCount, int fleetMax);

    void setExpeditionStatus(int fleetCount, int fleetMax);

    int getFleetFreeSlots();

    int getExpeditionFreeSlots();

    public int getFleetCount();

    public int getFleetMax();

    public int getExpeditionCount();

    public int getExpeditionMax();
}
