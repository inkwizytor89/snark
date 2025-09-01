package org.enoch.snark.instance.model.action.find;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.PlanetData;
import org.enoch.snark.instance.model.to.PlanetTerm;

import java.util.List;

import static org.enoch.snark.instance.si.module.ThreadMap.TRIP;

public class TripFinder {

    public static ColonyEntity next(ColonyEntity colonyEntity) {
        List<ColonyEntity> configTrip = Instance.getGlobalMainConfigMap().getColonies(TRIP, null);
        if(configTrip == null) throw new IllegalStateException("Missing config: "+TRIP);
        int index = configTrip.indexOf(colonyEntity);
        if(index == -1) {
            ColonyEntity swapColony = ColonyDAO.getInstance().find(colonyEntity.toPlanet().swapType());
            index = configTrip.indexOf(swapColony);
        }
        if(index==-1) return null;
        else return configTrip.get((index + 1) %  configTrip.size());
    }

    public static ColonyEntity prev(ColonyEntity colonyEntity) {
        List<ColonyEntity> configTrip = Instance.getGlobalMainConfigMap().getColonies(TRIP, null);
        if(configTrip == null) throw new IllegalStateException("Missing config: "+TRIP);
        int index = configTrip.indexOf(colonyEntity);
        if(index == -1) {
            ColonyEntity swapColony = ColonyDAO.getInstance().find(colonyEntity.toPlanet().swapType());
            index = configTrip.indexOf(swapColony);
        }
        if(index==-1) return null;
        else return configTrip.get((index + configTrip.size() -1) %  configTrip.size());
    }

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
