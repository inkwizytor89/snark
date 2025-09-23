package org.enoch.snark.common.time;

import org.enoch.snark.common.Parsable;

import java.util.Random;

public class Duration extends Parsable<java.time.Duration> {

    public static final String PT = "PT";

    private java.time.Duration base = java.time.Duration.ZERO;
    private java.time.Duration randomRange = java.time.Duration.ZERO;

    public Duration(String input) {
        super(input);
    }

    public Duration() {
        super("0S");
    }

    @Override
    public void setUp() {
        String[] durationParts = input.split("\\?");
        if(durationParts.length == 1) {
            base = java.time.Duration.parse(PT+input);
            randomRange = java.time.Duration.ZERO;
//            value = base;
        }
        else if (input.startsWith("?")) {
            base = java.time.Duration.ZERO;
            randomRange = java.time.Duration.parse(PT +durationParts[1]);
//            value = calculateRandom(0L, randomRange.getSeconds());
        } else {
            base = java.time.Duration.parse(PT+durationParts[0]);
            randomRange = java.time.Duration.parse(PT +durationParts[1]);
//            value = calculateRandom(base.getSeconds(), randomRange.getSeconds());
        }
    }

    @Override
    protected void randomize() {
        if(value != null && java.time.Duration.ZERO.equals(randomRange)) return;
        long randomValue = 0L;
        if(!java.time.Duration.ZERO.equals(randomRange)) {
            Random random = new Random();
            randomValue = random.nextLong(randomRange.getSeconds());
        }
        value =  java.time.Duration.ofSeconds(randomValue + base.getSeconds());
        expireDuration = value;
    }

    public long getSeconds() {
        return getValue().getSeconds();
    }
}
