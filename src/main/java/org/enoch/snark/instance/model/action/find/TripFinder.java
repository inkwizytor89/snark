package org.enoch.snark.instance.model.action.find;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.Instance;

import java.util.List;

import static org.enoch.snark.instance.si.module.ConfigMap.TRIP;

public class TripFinder {

    public static ColonyEntity next(ColonyEntity colonyEntity) {
        List<ColonyEntity> configTrip = Instance.getMainConfigMap().getColonies(TRIP, null);
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
        List<ColonyEntity> configTrip = Instance.getMainConfigMap().getColonies(TRIP, null);
        if(configTrip == null) throw new IllegalStateException("Missing config: "+TRIP);
        int index = configTrip.indexOf(colonyEntity);
        if(index == -1) {
            ColonyEntity swapColony = ColonyDAO.getInstance().find(colonyEntity.toPlanet().swapType());
            index = configTrip.indexOf(swapColony);
        }
        if(index==-1) return null;
        else return configTrip.get((index + configTrip.size() -1) %  configTrip.size());
    }
}
