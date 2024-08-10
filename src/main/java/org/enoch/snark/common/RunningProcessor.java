package org.enoch.snark.common;

import lombok.Getter;

import java.util.Arrays;

import static org.enoch.snark.common.RunningState.*;

public class RunningProcessor {
    private final RunningState[] loggingStates;
    RunningState prevState = RunningState.OFF;
    @Getter
    RunningState actualState = RunningState.OFF;

    public RunningProcessor() {
        this(STARTING, CLOSING);
    }

    public RunningProcessor(RunningState... loggingStates) {
        this.loggingStates = loggingStates;
    }

    public RunningProcessor update(boolean shouldRunning) {
        return update(shouldRunning, false);
    }

    public RunningProcessor update(boolean shouldRunning, boolean shouldPaused) {
        prevState = actualState;

        if(shouldRunning && shouldPaused) actualState = PAUSE_ON;
        else if(shouldRunning) actualState = ON;
        else actualState = RunningState.OFF;

        if(RunningState.isNotRunning(prevState) && RunningState.isRunning(actualState)) actualState = STARTING;
        else if(RunningState.isRunning(prevState) && RunningState.isNotRunning(actualState)) actualState = CLOSING;
        return this;
    }

    public RunningProcessor logChangedStatus(String name, Object... objects) {
        for (RunningState state : loggingStates)
            if(state.equals(actualState))
                logAction(name, state.name(), objects);
        return this;
    }

    private void logAction(String name, String action, Object[] objects) {
        StringBuilder builder = new StringBuilder(name + " " + action + " ");
        Arrays.asList(objects).forEach(builder::append);
        System.err.println(builder);
    }
}
