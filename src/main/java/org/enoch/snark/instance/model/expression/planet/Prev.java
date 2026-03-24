package org.enoch.snark.instance.model.expression.planet;

import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.model.expression.common.Trip;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.PlanetData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rotate right operation: [A,B,C] -> [C,A,B]
 */
public class Prev {

    private static ColonyRepository colonyRepository;

    public static synchronized void setRepository(ColonyRepository colonyRepo) {
        colonyRepository = colonyRepo;
    }

    public static Object execute(String current,String trip) {
        List<Planet> configTrip = Planet.fromString(trip);
        int index = Trip.currentTripIndex(Planet.parse(current), configTrip);
        return configTrip.get((configTrip.size()+ index - 1) %  configTrip.size());
    }
}

