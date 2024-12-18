package org.enoch.snark.common.time;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.si.module.ThreadMap.*;

public class TimePlan  {
    private final List<TimeInterval> activationTimes;

    public TimePlan(String input) {
        if(input == null || input.isEmpty() || input.contains(OFF))  activationTimes = new ArrayList<>();
        else if(input.contains(ON))  activationTimes = null;
        else {
            activationTimes = new ArrayList<>();
            List<String> timeList = Arrays.asList(input.split(ARRAY_SEPARATOR));
            timeList.forEach(singleRangeInput -> {
                String[] vars = singleRangeInput.split("-");
                if (vars.length == 2) {
                    LocalTime start = new Time(vars[0]).getValue();
                    LocalTime end = new Time(vars[1]).getValue();
                    activationTimes.add(new TimeInterval(start, end));
                }
            });
        }

    }

    public boolean isOn() {
        if(activationTimes == null) return true;
        else if(activationTimes.isEmpty()) return false;
        return activationTimes.stream().anyMatch(TimeInterval::isOn);
    }

    @Override
    public String toString() {
        if(activationTimes == null) return ON;
        if(activationTimes.isEmpty()) return OFF;
        return activationTimes.stream().map(TimeInterval::toString).collect(Collectors.joining(";"));
    }
}
