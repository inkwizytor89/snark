package org.enoch.snark.instance.model.expression.coordinate;

import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.model.to.Planet;

import java.util.List;

/**
 * Swap first two elements operation: [A,B,C] -> [B,A,C]
 */
public class Base {

    private static ColonyRepository colonyRepository;

    public static synchronized void setRepository(ColonyRepository colonyRepo) {
        colonyRepository = colonyRepo;
    }

    public static Object swap(String current) {
        List<Planet> planets = Planet.fromString(current);

        return planets.stream()
                .map(Planet::swapType)
                .toList();
    }

    public static Object space(String current) {
        return spaceSystem(current, "0");
    }

    public static Object spaceSystem(String current, String moveSystem) {
        List<Planet> planets = Planet.fromString(current);
        int i = Integer.parseInt(moveSystem);
        return planets.stream()
                .map(planet -> {
                    planet.system += i;
                    return planet.toSpace();
                }).toList();
    }
}

