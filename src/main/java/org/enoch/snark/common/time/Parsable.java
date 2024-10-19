package org.enoch.snark.common.time;

import org.enoch.snark.common.DateUtil;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

abstract class Parsable<V> {

    protected String input;
    private LocalDateTime lastUpdated;
    protected V value;

    public Parsable(String input) {
        init(input);
    }

    private void init(String input){
        this.input = input;
    }

    public void update(String input) {
        if(!input.equals(this.input)) {
            init(input);
            value = null;
        }
    }

    protected abstract void setUp();

    public V getValue() {
        if(value == null || DateUtil.isExpired(lastUpdated, 1L, ChronoUnit.DAYS)) {
            lastUpdated = LocalDateTime.now();
            setUp();
        }
        return value;
    }
}
