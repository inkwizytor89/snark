package org.enoch.snark.common.time;

import org.enoch.snark.common.Parsable;
import org.enoch.snark.common.RunningState;

public class TimeScheduler extends Parsable<TimePlan> {

    public TimeScheduler(String input) {
        super(input);
    }

    @Override
    protected void setUp() {
        value = new TimePlan(input);
    }

    public boolean isOn() {
        return getValue().isOn();
    }

    public RunningState getRunningState() {

        return value == null ? RunningState.ON : value.getRunningState();
    }

    @Override
    public String toString() {
        return getRunningState().name();
    }
}
