package org.enoch.snark.common;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static LocalDateTime parseToLocalDateTime(String input) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss");
        final String[] split = input.split("\\s+");
        String date = split[0];
        String time = split[1];
        if(time.length() == 7) {
            time = "0"+time;
        }
        return LocalDateTime.parse(date + " " + time, formatter);
    }

    // TODO: 2018-02-06 extends to days in input
    public static LocalTime parse(String input) {
        final String[] split = input.split("\\s+");
        String time = split[0];
        if(time.startsWith("0")) {
            time = "0"+time;
        }
        return LocalTime.parse(time);
    }

    public static boolean lessThan20H(LocalDateTime updated) {
        LocalDateTime now = LocalDateTime.now();
        return now.minusHours(20).isBefore(updated);
    }

}
