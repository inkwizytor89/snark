package org.enoch.snark.common.time;

import lombok.AllArgsConstructor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.si.module.ConfigMap.*;

public class GameRangeTime extends Parsable<Boolean> {
    private List<RangeTime> activationTimes = new ArrayList<>();

    public GameRangeTime(String input) {
        super(input);
    }

    @Override
    protected void setUp() {
        if(input == null || input.isEmpty() || input.contains(OFF))  activationTimes = new ArrayList<>();
        else if(input.contains(ON))  activationTimes = null;
        else {
            activationTimes = new ArrayList<>();
            List<String> timeList = Arrays.asList(input.split(ARRAY_SEPARATOR));
            timeList.forEach(singleRangeInput -> {
                String[] vars = singleRangeInput.split("-");
                if (vars.length == 2) {
                    LocalTime start = new GameTime(vars[0]).getValue();
                    LocalTime end = new GameTime(vars[1]).getValue();
                    activationTimes.add(new RangeTime(start, end));
                }
            });
        }
        if(activationTimes == null) value = true;
        else if(activationTimes.isEmpty()) value = false;
        else value = activationTimes.stream().anyMatch(RangeTime::isOn);
    }

    public boolean isOn() {
        return getValue();
    }

    @Override
    public String toString() {
        if(activationTimes == null) return ON;
        if(activationTimes.isEmpty()) return OFF;
        return activationTimes.stream().map(RangeTime::toString).collect(Collectors.joining(";"));
    }

    @AllArgsConstructor
    class RangeTime {
        LocalTime start;
        LocalTime end;

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
}
