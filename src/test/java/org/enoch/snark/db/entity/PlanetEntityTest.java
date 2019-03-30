package org.enoch.snark.db.entity;

import org.junit.Test;

import static org.junit.Assert.*;

public class PlanetEntityTest {

    @Test
    public void parseResource_inSampleFormat() {
        String input = "274.884";

        Long result = PlanetEntity.parseResource(input);

        assertEquals((Long)274884L, result);
    }

    @Test
    public void parseResource_inMlnFormat() {
        String input = "2,839Mln";

        Long result = PlanetEntity.parseResource(input);

        assertEquals((Long)2839000L, result);
    }
}