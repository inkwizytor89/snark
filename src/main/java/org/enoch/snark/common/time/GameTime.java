package org.enoch.snark.common.time;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class GameTime extends Parsable<LocalTime> {

    public static final int DURATION_PART_INDEX = 1;
    private static final int TIME_PART_INDEX = 0;

    public static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

    public GameTime(String input) {
        super(input);
    }

    @Override
    protected void setUp() {
        String[] timeParts = input.split("\\?");
        Duration duration = new Duration();
        if(timeParts.length > 1)
            duration = new Duration("?"+timeParts[DURATION_PART_INDEX]);

        LocalTime baseTime = LocalTime.parse(timeParts[TIME_PART_INDEX], dtf);
        value = baseTime.plusSeconds(duration.getValue().getSeconds());
    }
}
