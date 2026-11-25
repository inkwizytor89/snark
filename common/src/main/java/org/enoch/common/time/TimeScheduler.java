package org.enoch.common.time;

import org.enoch.common.Parsable;
import org.enoch.common.RunningState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimeScheduler extends Parsable<TimePlan> {

    private static Map<String, TimeScheduler> cache = new ConcurrentHashMap<>();

    public TimeScheduler(String input) {
        super(input);
    }

    public static TimeScheduler parse(String input) {
        return cache.computeIfAbsent(input, TimeScheduler::new);
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
