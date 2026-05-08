package org.enoch.snark.instance.model.expression.coordinate;

import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.PlanetData;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.service.PlanetDataService;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Swap first two elements operation: [A,B,C] -> [B,A,C]
 */
public class Colonies {

    private static final String PLANETS = "PLANETS";
    private static final String MOONS = "MOONS";
    private static final String ALL = "ALL";
    private static final String EACH_POSITION = EMPTY;
    private static final String NONE = "NONE";

    private static ColonyRepository colonyRepository;
    private static PlanetDataService planetDataService;

    public static synchronized void setRepository(ColonyRepository colonyRepo) {
        colonyRepository = colonyRepo;
    }

    public static void setRepository(PlanetDataService planetDataService) {
        Colonies.planetDataService = planetDataService;
    }

    public static List<PlanetData> planets() {
        return colonyRepository.findByCode(PLANETS).stream()
                .map(PlanetData::new)
                .toList();
    }
    public static List<PlanetData> planets(List<PlanetData> coordinateList) {
        String coordinates = coordinateList.stream()
                .map(planetData -> planetData.getPlanet().toString())
                .collect(java.util.stream.Collectors.joining(";"));
        return planets(coordinates);
    }
    public static List<PlanetData> planets(String coordinatesString) {
        String switchToMoons= Planet.fromString(coordinatesString).stream()
                .map(coordinate -> {
                    if(coordinate.is(ColonyType.PLANET)) return coordinate.toString();
                    else return coordinate.swapType().toString();
                })
                .collect(java.util.stream.Collectors.joining(";"));
        return planetDataService.fetchCoordinates(switchToMoons);
    }
    public static List<PlanetData> moons() {
        return colonyRepository.findByCode(MOONS).stream()
                .map(PlanetData::new)
                .toList();
    }
    public static List<PlanetData> moons(List<PlanetData> coordinateList) {
        String coordinates = coordinateList.stream()
                .map(planetData -> planetData.getPlanet().toString())
                .collect(java.util.stream.Collectors.joining(";"));
        return moons(coordinates);
    }
    public static List<PlanetData> moons(String coordinatesString) {
        String switchToMoons= Planet.fromString(coordinatesString).stream()
                .map(coordinate -> {
                    if(coordinate.is(ColonyType.MOON)) return coordinate.toString();
                    else return coordinate.swapType().toString();
                })
                .collect(java.util.stream.Collectors.joining(";"));
        return planetDataService.fetchCoordinates(switchToMoons);
    }

    public static List<PlanetData> all() {
        return colonyRepository.findByCode(ALL).stream()
                .map(PlanetData::new)
                .toList();
    }

    public static List<PlanetData> allPositions() {
        return colonyRepository.findByCode(EACH_POSITION).stream()
                .map(PlanetData::new)
                .toList();
    }

    public static List<PlanetData> none() {
        return new ArrayList<>();
    }

}

