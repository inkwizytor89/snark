package org.enoch.snark.common.time;

import lombok.Getter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class GameTime extends InputUpdater{

    public static final int DURATION_PART_INDEX = 1;
    private static final int TIME_PART_INDEX = 0;

    public static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
    @Getter
    private LocalTime time;
    private GameDuration duration;

    public GameTime(String input) {
        super(input);
    }

    @Override
    protected void setUp() {
        String[] timeParts = input.split("\\?");
        duration = new GameDuration();
        if(timeParts.length > 1)
            duration = new GameDuration("?"+timeParts[DURATION_PART_INDEX]);

        LocalTime baseTime = LocalTime.parse(timeParts[TIME_PART_INDEX], dtf);
        time = baseTime.plusSeconds(duration.getSeconds());
    }
}
