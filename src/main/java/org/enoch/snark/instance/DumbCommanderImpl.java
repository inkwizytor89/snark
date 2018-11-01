package org.enoch.snark.instance;

import org.enoch.snark.gi.command.AbstractCommand;

public class DumbCommanderImpl implements Commander {

    @Override
    public void push(AbstractCommand command) {

    }

    @Override
    public void setFleetStatus(int fleetCount, int fleetMax) {

    }

    @Override
    public void setExpeditionStatus(int fleetCount, int fleetMax) {

    }

    @Override
    public int getFleetFreeSlots() {
        return 0;
    }

    @Override
    public int getExpeditionFreeSlots() {
        return 0;
    }

    @Override
    public int getFleetCount() {
        return 0;
    }

    @Override
    public int getFleetMax() {
        return 0;
    }

    @Override
    public int getExpeditionCount() {
        return 0;
    }

    @Override
    public int getExpeditionMax() {
        return 0;
    }
}
