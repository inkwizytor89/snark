package org.enoch.snark.instance.model.action.find;

import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.PlanetData;
import org.enoch.snark.instance.model.to.PlanetTerm;

import java.util.List;

public class TripFinder {

    public static PlanetData next(PlanetTerm term) {
        int index = currentTripIndex(term);
        List<PlanetData> trip = term.getContext().getTrip();
        return trip.get((index + 1) %  trip.size());
    }

    public static PlanetData prev(PlanetTerm term) {
        int index = currentTripIndex(term);
        List<PlanetData> trip = term.getContext().getTrip();
        return trip.get((index - 1) %  trip.size());
    }

    public static int currentTripIndex(PlanetTerm term) {
        List<PlanetData> configTrip = term.getContext().getTrip();
        if(configTrip == null) throw new IllegalStateException("Missing trip for "+term);
        int index = indexOf(configTrip, term.getPlanetData().getPlanet());
        if(index == -1) throw new IllegalStateException("Missing planet "+term.getPlanetData().getPlanet() + " in trip "+configTrip);
        return index;
    }

    private static int indexOf(List<PlanetData> list, Planet planet) {
        int index = 0;
        for(PlanetData planetData : list) {
            if(planetData.getPlanet().equals(planet)) return index;
            index++;
        }
        return -1;
    }
}
