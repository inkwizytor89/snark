package org.enoch.snark.common;

import java.util.Arrays;

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

    public void log(String name, Object... objects) {
        if(shouldStart()) {
            logAction(name, "start", objects);
        } else if(shouldStop()) {
            logAction(name, "stop", objects);
        }
    }

    public void logAction(String name, String action, Object[] objects) {
        StringBuilder builder = new StringBuilder(name + " " + action + " ");
        Arrays.asList(objects).forEach(builder::append);
        System.err.println(builder);
    }
}
