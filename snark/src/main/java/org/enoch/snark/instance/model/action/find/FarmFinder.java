package org.enoch.snark.instance.model.action.find;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.model.to.Planet;

import java.util.List;

public class FarmFinder {
    public static List<Planet> find(ColonyEntity colonyEntity) {
//        Integer explorationArea = Instance.getGlobalMainConfigMap().getConfigInteger(EXPLORATION_AREA, 100);
//        List<TargetEntity> farms = TargetDAO.getInstance().findFarms(colonyEntity.galaxy, colonyEntity.system, explorationArea);
//        System.err.println(farms.size()+" farms: "+farms.stream().map(PlanetEntity::toPlanet).map(Planet::toString).collect(Collectors.joining(", ")));
//        return farms.stream().map(PlanetEntity::toPlanet).toList();
        return null;
    }
}
