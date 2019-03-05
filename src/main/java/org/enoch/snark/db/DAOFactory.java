package org.enoch.snark.db;

import org.enoch.snark.db.dao.*;
import org.enoch.snark.db.dao.impl.*;
import org.enoch.snark.db.entity.UniverseEntity;

import javax.annotation.Nonnull;

public class DAOFactory {
    public FarmDAO farmDAO;
    public FleetDAO fleetDAO;
    public FleetRequestDAO fleetRequestDAO;
    public GalaxyDAO galaxyDAO;
    public PlanetDAO planetDAO;
    public SourceDAO sourceDAO;

    public DAOFactory(@Nonnull UniverseEntity entity) {
        farmDAO = new FarmDAOImpl(entity);
        fleetDAO = new FleetDAOImpl(entity);
        fleetRequestDAO = new FleetRequestDAOImpl(entity);
        galaxyDAO = new GalaxyDAOImpl(entity);
        planetDAO = new PlanetDAOImpl(entity);
        sourceDAO = new SourceDAOImpl(entity);
    }

}
