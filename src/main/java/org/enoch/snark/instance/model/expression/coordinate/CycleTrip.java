package org.enoch.snark.instance.model.expression.coordinate;

import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.model.expression.common.Trip;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.PlanetData;
import org.enoch.snark.instance.service.PlanetDataService;

import java.util.List;

/**
 * Take next in list C:[A,B,C] -> A
 */
public class CycleTrip {

    private static PlanetDataService planetDataService;

    public static void setRepository(PlanetDataService planetDataService) {
        CycleTrip.planetDataService = planetDataService;
    }

    public static List<PlanetData> next(List<PlanetData> current, String trip) {
        return next(current.getFirst().getPlanet().toString(), trip);
    }

    public static List<PlanetData> next(String current, String trip) {
        List<Planet> configTrip = Planet.fromString(trip);
        int index = Trip.currentTripIndex(Planet.parse(current), configTrip);
        Planet nextPlanet = configTrip.get((index + 1) % configTrip.size());
        return planetDataService.fetchCoordinates(nextPlanet.toString());
    }

    public static List<PlanetData> prev(List<PlanetData> current, String trip) {
        return prev(current.getFirst().getPlanet().toString(), trip);
    }

    public static List<PlanetData> prev(String current,String trip) {
        List<Planet> configTrip = Planet.fromString(trip);
        int index = Trip.currentTripIndex(Planet.parse(current), configTrip);
        Planet prevPlanet = configTrip.get((configTrip.size() + index - 1) % configTrip.size());
        return planetDataService.fetchCoordinates(prevPlanet.toString());
    }
}

