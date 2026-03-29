package org.enoch.snark.instance.model.expression.coordinate;

import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.model.expression.common.Trip;
import org.enoch.snark.instance.model.to.Planet;

import java.util.List;

/**
 * Take next in list C:[A,B,C] -> A
 */
public class CycleTrip {

    private static ColonyRepository colonyRepository;

    public static synchronized void setRepository(ColonyRepository colonyRepo) {
        colonyRepository = colonyRepo;
    }

    public static Object next(String current,String trip) {
        List<Planet> configTrip = Planet.fromString(trip);
        int index = Trip.currentTripIndex(Planet.parse(current), configTrip);
        return configTrip.get((index + 1) %  configTrip.size());
    }

    public static Object prev(String current,String trip) {
        List<Planet> configTrip = Planet.fromString(trip);
        int index = Trip.currentTripIndex(Planet.parse(current), configTrip);
        return configTrip.get((configTrip.size()+ index - 1) %  configTrip.size());
    }
}

