package org.enoch.snark.instance.model.expression.coordinate;

import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.PlanetData;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Swap first two elements operation: [A,B,C] -> [B,A,C]
 */
public class Base {

    private static ColonyRepository colonyRepository;

    public static synchronized void setRepository(ColonyRepository colonyRepo) {
        colonyRepository = colonyRepo;
    }

    public static List<PlanetData> swap(List<PlanetData> coordinateList) {
        String coordinates = coordinateList.stream()
                .map(planetData -> planetData.getPlanet().toString())
                .collect(java.util.stream.Collectors.joining(";"));
        return swap(coordinates);
    }
    public static List<PlanetData> swap(String planetString) {
        String swappedCoordinates= Planet.fromString(planetString).stream()
                .map(Planet::swapType)
                .map(Planet::toString)
                .collect(java.util.stream.Collectors.joining(";"));
        return colonyRepository.fromColoniesList(swappedCoordinates).stream()
                .map(PlanetData::new)
                .toList();
    }

    public static List<PlanetData> space(List<PlanetData> coordinateList, String moveSystem) {
        String coordinates = coordinateList.stream()
                .map(planetData -> planetData.getPlanet().toString())
                .collect(java.util.stream.Collectors.joining(";"));
        return space(coordinates, moveSystem);
    }

    public static List<PlanetData> space(String coordinateString, String moveSystem) {
        int i = Integer.parseInt(moveSystem);
        String spaceCoordinates = Planet.fromString(coordinateString).stream()
                .map(planet -> {
                    planet.system += i;
                    return planet.toSpace();
                })
                .map(Planet::toString)
                .collect(Collectors.joining(";"));
        return colonyRepository.fromColoniesList(spaceCoordinates).stream()
                .map(PlanetData::new)
                .toList();
    }
}
