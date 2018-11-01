package org.enoch.snark.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class SourcePlanetTest {

    @Test
    public void initClass_when_comesString() {
        final String input = "[1:1:1]#1";

        final SourcePlanet sourcePlanet = new SourcePlanet(input);

        Integer expected = 1;
        assertEquals(expected, sourcePlanet.planetId);
        assertEquals(expected, sourcePlanet.galaxy);
        assertEquals(expected, sourcePlanet.system);
        assertEquals(expected, sourcePlanet.position);
    }
}