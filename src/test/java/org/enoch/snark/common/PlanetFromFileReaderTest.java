package org.enoch.snark.common;

import org.enoch.snark.instance.model.to.Planet;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class PlanetFromFileReaderTest {

    private static String PATH_TO_TEST_RESOURCE = "src/test/resources/";

    @Test
    public void get() {

        final List<Planet> targetPlanets = PlanetFromFileReader.get(PATH_TO_TEST_RESOURCE+"FarmModule/targets.txt");

        assertEquals(4, targetPlanets.size());
    }
}