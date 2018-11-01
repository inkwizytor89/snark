package org.enoch.snark.common;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TimeZone;

public class DateUtil {

    // TODO: 2018-02-06 extends to days in input
    public static LocalTime parse(String input) {
        final String[] split = input.split("\\s+");
        String time = split[0];
        if(time.startsWith("0")) {
            time = "0"+time;
        }
        return LocalTime.parse(time);

    }

    public static boolean lessThan20H(Timestamp updated) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastUpdate = LocalDateTime.ofInstant(Instant.ofEpochMilli(updated.getTime() ), TimeZone
                .getDefault().toZoneId());
        return now.minusHours(20).isBefore(lastUpdate);
    }

}
