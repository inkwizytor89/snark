package org.enoch.snark.instance.model.action.find;

import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.model.to.Planet;

import java.util.List;
import java.util.stream.Collectors;

public class FarmFinder {
    public static Planet find(ColonyEntity colonyEntity) {
        List<TargetEntity> farms = TargetDAO.getInstance().findFarms(colonyEntity.galaxy, colonyEntity.system, 100);
        System.err.println(farms.size()+" farms: "+farms.stream().map(PlanetEntity::toPlanet).map(Planet::toString).collect(Collectors.joining(", ")));
        return farms.isEmpty() ? null : farms.get(0).toPlanet();
    }
}
