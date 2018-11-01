package org.enoch.snark.model;

import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;

public class SpyInfoTest {

    @Test
    public void sampleTest() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        final SpyInfo info = new SpyInfo( new File("src/test/resources/[1_2_3]#6479460"));

        LocalDateTime expectedDate = LocalDateTime.parse("09.02.2018 09:07:06", formatter);
        assertEquals(expectedDate, info.date);
        assertEquals(new Planet("[[3:104:7]]"), info.planet);
        assertEquals(26811L, info.metal.longValue());
        assertEquals(10534L, info.crystal.longValue());
        assertEquals(1725L, info.deuterium.longValue());
        assertEquals(2324L, info.power.longValue());

    }


}