package org.enoch.snark.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class PlanetTest {

    @Test
    public void initClass_when_comesString() {
        final String input = "[1:1:1]";

        final Planet sourcePlanet = new Planet(input);

        Integer expected = 1;
        assertEquals(expected, sourcePlanet.galaxy);
        assertEquals(expected, sourcePlanet.system);
        assertEquals(expected, sourcePlanet.position);
    }

    @Test
    public void initClass_when_incorrectString() {
        final String input = "-[2:2:2]";

        final Planet sourcePlanet = new Planet(input);

        Integer expected = 2;
        assertEquals(expected, sourcePlanet.galaxy);
        assertEquals(expected, sourcePlanet.system);
        assertEquals(expected, sourcePlanet.position);
    }

}