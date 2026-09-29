package org.enoch.snark.common;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class Parsable<V> {

    protected String input;
    private LocalDateTime lastUpdated;
    protected Duration expireDuration = Duration.ofDays(1);
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

    /**
     * For init values in subclass from string
     */
    protected abstract void setUp();

    public V getValue() {
        if(value == null) {
            setUp();
            randomize();
            lastUpdated = LocalDateTime.now();
        } else if(DateUtil.isExpired(lastUpdated, expireDuration)) {
            randomize();
            lastUpdated = LocalDateTime.now();
        }
        return value;
    }

    /**
     * For set and reload values after expire duration (Default 1 day).
     * Not needed if the value will not change cyclically
     */
    protected void randomize() {
    }

    @Override
    public String toString() {
        return input;
    }
}
