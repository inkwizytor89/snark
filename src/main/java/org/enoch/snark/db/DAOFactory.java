package org.enoch.snark.db;

import org.enoch.snark.db.dao.*;
import org.enoch.snark.db.dao.impl.*;
import org.enoch.snark.db.entity.UniverseEntity;

import javax.annotation.Nonnull;

public class DAOFactory {
    public FarmDAO farmDAO;
    public FleetDAO fleetDAO;
    public GalaxyDAO galaxyDAO;
    public TargetDAO targetDAO;
    public SourceDAO sourceDAO;

    public DAOFactory(@Nonnull UniverseEntity entity) {
        farmDAO = new FarmDAOImpl(entity);
        fleetDAO = new FleetDAOImpl(entity);
        galaxyDAO = new GalaxyDAOImpl(entity);
        targetDAO = new TargetDAOImpl(entity);
        sourceDAO = new SourceDAOImpl(entity);
    }

}
