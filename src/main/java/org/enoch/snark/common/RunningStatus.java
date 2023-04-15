package org.enoch.snark.common;

public class RunningStatus {

    private final boolean oldStatus;
    private final boolean newStatus;

    public RunningStatus(boolean oldStatus, boolean newStatus) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public boolean shouldRunning() {
        return newStatus;
    }

    public boolean shouldStart() {
        return !oldStatus && newStatus;
    }

    public boolean shouldStop() {
        return oldStatus && !newStatus;
    }

    public void log(String name) {
        if(shouldStart()) {
            System.err.println(name + " start");
        } else if(shouldStop()) {
            System.err.println(name + " stop");
        }
    }
}
