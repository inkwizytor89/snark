package org.enoch.snark.common.time;

import org.enoch.snark.common.Parsable;

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

    @Override
    public String toString() {
        return value.toString();
    }
}
