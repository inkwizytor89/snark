package org.enoch.common.time;

import java.time.LocalTime;

public class TimeInterval {
    LocalTime start;
    LocalTime end;

    public TimeInterval(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public boolean isOn() {
        return (nowIsInMiddle(start, end) && start.isBefore(end)) ||
                (!nowIsInMiddle(end, start) && start.isAfter(end));
    }

    private boolean nowIsInMiddle(LocalTime start, LocalTime end) {
        LocalTime now = LocalTime.now();
        return now.isAfter(start) && now.isBefore(end);
    }

    @Override
    public String toString() {
        return start + "-" +end;
    }
}
