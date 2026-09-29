package org.enoch.snark.expression.coordinate.common;

import org.enoch.snark.instance.model.to.Planet;

import java.util.List;

public class Trip {


    public static int currentTripIndex(Planet planet, List<Planet> configTrip) {
//        List<PlanetData> configTrip = term.getContext().getTrip();
        int index = indexOf(planet, configTrip);
        if(index == -1) throw new IllegalStateException("Missing planet "+planet + " in trip "+configTrip);
        return index;
    }


    private static int indexOf(Planet planet, List<Planet> list) {
        int index = 0;
        for(Planet planetData : list) {
            if(planetData.equals(planet)) return index;
            index++;
        }
        return -1;
    }
}
