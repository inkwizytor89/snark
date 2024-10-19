package org.enoch.snark.common.time;

import java.util.Random;

public class Duration extends Parsable<java.time.Duration> {

    public static final String PT = "PT";

    public Duration(String input) {
        super(input);
    }

    public Duration() {
        super("0S");
    }

    @Override
    public void setUp() {
        String[] durationParts = input.split("\\?");
        if(durationParts.length == 1) value = java.time.Duration.parse(PT+input);
        else if (input.startsWith("?")) {
            value = calculateRandom(0L, java.time.Duration.parse(PT +durationParts[1]).getSeconds());
        } else {
            value = calculateRandom(java.time.Duration.parse(PT+durationParts[0]).getSeconds(),
                    java.time.Duration.parse(PT+durationParts[1]).getSeconds());
        }
    }

    private java.time.Duration calculateRandom(long from, long to) {
        Random random = new Random();
        return java.time.Duration.ofSeconds(random.nextLong(to - from) + from);
    }

    public long getSeconds() {
        return getValue().getSeconds();
    }
}
