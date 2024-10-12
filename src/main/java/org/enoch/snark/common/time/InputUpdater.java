package org.enoch.snark.common.time;

import org.enoch.snark.common.DateUtil;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

abstract class InputUpdater {

    protected String input;
    private LocalDateTime lastUpdated;

    protected InputUpdater(String input) {
        init(input);
        setUp();
    }

    private void init(String input){
        this.input = input;
        lastUpdated = LocalDateTime.now();
    }

    public void update(String input) {
        if(!input.equals(this.input) || DateUtil.isExpired(lastUpdated, 1L, ChronoUnit.DAYS)) {
            init(input);
            setUp();
        }
    }

    protected abstract void setUp();
}
