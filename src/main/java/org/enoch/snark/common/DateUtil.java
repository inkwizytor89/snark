package org.enoch.snark.common;

import org.enoch.snark.db.dao.CacheEntryDAO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

import static org.enoch.snark.db.entity.CacheEntryEntity.HIGH_SCORE;

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

    // TODO: 2018-02-06 extends to days in input 1:05:34 h
    // dobrze 0:10:21 h
    public static LocalTime parseDuration(String input) {
        final String[] split = input.split("\\s+");
        String time = split[0];
        if(time.length() < 8) {
            time = "0"+time;
        }
        return LocalTime.parse(time);
    }

    public static LocalTime parseStringToTime(String input) {
        final String[] split = input.split("\\s+");
        String time = split[0];
        return LocalTime.parse(time);
    }

    public static LocalDateTime parseTimeToDateTime(LocalTime input) {
        LocalDate now = LocalDate.now();
        if(LocalTime.now().isAfter(input)) {
            now = now.plusDays(1);
        }
        return LocalDateTime.of(now, input);
    }

    public static LocalDateTime parseStringTimeToDateTime(String date) {
        return parseTimeToDateTime(parseStringToTime(date));
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

    public static boolean isExpired(String cacheEntryKey, long amountToAdd, TemporalUnit unit) {
        return isExpired(CacheEntryDAO.getInstance().getDate(cacheEntryKey), amountToAdd, unit);
    }

    public static boolean isExpired(LocalDateTime date, long amountToAdd, TemporalUnit unit) {
        if (date == null) return true;
        else return date.plus(amountToAdd, unit).isBefore(LocalDateTime.now());
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
