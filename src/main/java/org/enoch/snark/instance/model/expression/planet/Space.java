package org.enoch.snark.instance.model.expression.planet;

import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.model.to.Planet;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Swap first two elements operation: [A,B,C] -> [B,A,C]
 */
public class Space {

    private static ColonyRepository colonyRepository;

    public static synchronized void setRepository(ColonyRepository colonyRepo) {
        colonyRepository = colonyRepo;
    }


    public static Object execute(String current) {
        return execute(current, "0");
    }

    public static Object execute(String current, String moveSystem) {
        List<Planet> planets = Planet.fromString(current);
        int i = Integer.parseInt(moveSystem);
        return planets.stream()
                .map(planet -> {
                    planet.system += i;
                    return planet.toSpace();
                }).toList();
    }
}

