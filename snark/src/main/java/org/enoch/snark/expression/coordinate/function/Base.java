package org.enoch.snark.expression.coordinate.function;

import org.enoch.snark.db.entity.CacheEntryEntity;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.PlanetData;
import org.enoch.snark.instance.service.PlanetDataService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Swap first two elements operation: [A,B,C] -> [B,A,C]
 */
public class Base {

    private static PlanetDataService planetDataService;
    private static CacheEntryRepository cacheEntryRepository;

    public static void setRepository(PlanetDataService planetDataService, CacheEntryRepository cacheEntryRepository) {
        Base.planetDataService = planetDataService;
        Base.cacheEntryRepository = cacheEntryRepository;
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
        return planetDataService.fetchCoordinates(swappedCoordinates);
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
        return planetDataService.fetchCoordinates(spaceCoordinates);
    }

    public static List<PlanetData> cacheKey(String cacheEntryKey) {
        Optional<CacheEntryEntity> optionalEntry = cacheEntryRepository.findByKey(cacheEntryKey);
        if (optionalEntry.isEmpty()) throw new IllegalArgumentException("CoordinateService failure. No cache entry found for key: "+cacheEntryKey);
        String spaceCoordinates = Planet.fromString(optionalEntry.get().value).stream()
                .map(Planet::toString)
                .collect(Collectors.joining(";"));
        return planetDataService.fetchCoordinates(spaceCoordinates);
    }
}
