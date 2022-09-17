package org.enoch.snark.common;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static LocalDateTime parseToLocalDateTime(String input) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss");
        final String[] split = input.split("\\s+");
        if(split.length < 2) System.err.println("Inpot was: "+input+" and first element "+split[0]);
        String date = split[0];
        String time = split[1];
        if(time.length() == 7) {
            time = "0"+time;
        }
        return LocalDateTime.parse(date + " " + time, formatter);
    }

    // TODO: 2018-02-06 extends to days in input
    public static LocalTime parseDuration(String input) {
        final String[] split = input.split("\\s+");
        String time = split[0];
        if(time.startsWith("0")) {
            time = "0"+time;
        }
        return LocalTime.parse(time);
    }

    public static LocalTime parseTime(String input) {
        final String[] split = input.split("\\s+");
        String time = split[0];
        return LocalTime.parse(time);
    }

    /**
     *
     * @param input 4h 27m 10s
     * @return second
     */
    public static Integer parseCountDownToSec(String input) {
        final String[] split = input.split("\\D+");
        Integer result = 0;
        Integer multiply = 1;
        for(int i=1;i<=split.length;i++) {
            result = result + (Integer.parseInt(split[split.length-i]) * multiply);
            multiply = multiply * 60;
        }
        return result;
    }

    public static boolean lessThanHours(int hour, LocalDateTime updated) {
        LocalDateTime now = LocalDateTime.now();
        return now.minusHours(hour).isBefore(updated);
    }

    public static boolean lessThanDays(int days, LocalDateTime updated) {
        LocalDateTime now = LocalDateTime.now();
        return now.minusDays(days).isBefore(updated);
    }

}
