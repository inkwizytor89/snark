package org.enoch.snark.instance.model.expression.planet;

import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.model.expression.common.Trip;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.PlanetData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Swap first two elements operation: [A,B,C] -> [B,A,C]
 */
public class Swap {

    private static ColonyRepository colonyRepository;

    public static synchronized void setRepository(ColonyRepository colonyRepo) {
        colonyRepository = colonyRepo;
    }


    public static Object execute(String current) {
        List<Planet> planets = Planet.fromString(current);

        return planets.stream()
                .map(Planet::swapType)
                .toList();
    }
}

