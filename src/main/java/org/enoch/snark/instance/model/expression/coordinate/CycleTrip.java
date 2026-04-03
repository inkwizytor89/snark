package org.enoch.snark.instance.model.expression.coordinate;

import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.model.expression.common.Trip;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.PlanetData;

import java.util.List;

/**
 * Take next in list C:[A,B,C] -> A
 */
public class CycleTrip {

    private static ColonyRepository colonyRepository;

    public static synchronized void setRepository(ColonyRepository colonyRepo) {
        colonyRepository = colonyRepo;
    }

    public static List<PlanetData> next(List<PlanetData> current, String trip) {
        return next(current.getFirst().getPlanet().toString(), trip);
    }

    public static List<PlanetData> next(String current, String trip) {
        List<Planet> configTrip = Planet.fromString(trip);
        int index = Trip.currentTripIndex(Planet.parse(current), configTrip);
        Planet nextPlanet = configTrip.get((index + 1) % configTrip.size());
        return colonyRepository.fromColoniesList(nextPlanet.toString()).stream()
                .map(PlanetData::new)
                .toList();
    }

    public static List<PlanetData> prev(List<PlanetData> current, String trip) {
        return prev(current.getFirst().getPlanet().toString(), trip);
    }

    public static List<PlanetData> prev(String current,String trip) {
        List<Planet> configTrip = Planet.fromString(trip);
        int index = Trip.currentTripIndex(Planet.parse(current), configTrip);
        Planet prevPlanet = configTrip.get((configTrip.size() + index - 1) % configTrip.size());
        return colonyRepository.fromColoniesList(prevPlanet.toString()).stream()
                .map(PlanetData::new)
                .toList();
    }
}

