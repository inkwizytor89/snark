package org.enoch.snark.common;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public abstract class Parsable<V> {

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
            setUp();
            lastUpdated = LocalDateTime.now();
        }
        return value;
    }
}
