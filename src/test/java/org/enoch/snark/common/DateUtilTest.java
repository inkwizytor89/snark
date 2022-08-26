package org.enoch.snark.common;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.Assert.assertEquals;

public class DateUtilTest {

    @Test
    public void getDateFromTimeSreing() {
        String timeInput="21:01:19 h";

        final LocalTime time = DateUtil.parseDuration(timeInput);

        assertEquals(21,time.getHour());
        assertEquals(1,time.getMinute());
        assertEquals(19,time.getSecond());
    }

    @Test
    public void getShortedDateFromTimeSreing() {
        String timeInput="0:00:19";

        final LocalTime time = DateUtil.parseDuration(timeInput);

        assertEquals(0,time.getHour());
        assertEquals(0,time.getMinute());
        assertEquals(19,time.getSecond());
    }

    @Test
    public void parseOneNumberHour() {
        String date = "31.03.19 8:01:15";

        LocalDateTime localDateTime = DateUtil.parseToLocalDateTime(date);

        assertEquals(31, localDateTime.getDayOfMonth());
        assertEquals(3, localDateTime.getMonthValue());
        assertEquals(2019, localDateTime.getYear());
        assertEquals(8, localDateTime.getHour());
        assertEquals(1, localDateTime.getMinute());
        assertEquals(15, localDateTime.getSecond());
    }

    @Test
    public void parseTwoNumberHour() {
        String date = "31.03.19 18:01:15";

        LocalDateTime localDateTime = DateUtil.parseToLocalDateTime(date);

        assertEquals(31, localDateTime.getDayOfMonth());
        assertEquals(3, localDateTime.getMonthValue());
        assertEquals(2019, localDateTime.getYear());
        assertEquals(18, localDateTime.getHour());
        assertEquals(1, localDateTime.getMinute());
        assertEquals(15, localDateTime.getSecond());
    }
}