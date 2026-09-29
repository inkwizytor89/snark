package org.enoch.snark.common;

public enum RunningState {
    OFF,
    STARTING,
    ON,
    CLOSING,
    PAUSE_ON,
    PAUSE_OFF;

    public static boolean isRunning(RunningState state) {
        return STARTING.equals(state) || ON.equals(state);
    }

    public static boolean isNotRunning(RunningState state) {
        return !isRunning(state);
    }
}
