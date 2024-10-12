package org.enoch.snark.common.time;

import java.time.Duration;
import java.util.Random;

public class GameDuration extends InputUpdater {

    public static final String PT = "PT";
    private Duration duration;

    public GameDuration(String input) {
        super(input);
    }

    public GameDuration() {
        super("0S");
    }

    @Override
    public void setUp() {
        String[] durationParts = input.split("\\?");
        if(durationParts.length == 1) duration = Duration.parse(PT+input);
        else if (input.startsWith("?")) {
            duration = calculateRandom(0L, Duration.parse(PT +durationParts[1]).getSeconds());
        } else {
            duration = calculateRandom(Duration.parse(PT+durationParts[0]).getSeconds(),
                    Duration.parse(PT+durationParts[1]).getSeconds());
        }
    }

    private Duration calculateRandom(long from, long to) {
        Random random = new Random();
        return Duration.ofSeconds(random.nextLong(to - from) + from);
    }

    public long getSeconds() {
        return duration.getSeconds();
    }
}
