package org.enoch.snark.db;

import org.enoch.snark.db.dao.*;
import org.enoch.snark.db.dao.impl.*;
import org.enoch.snark.db.entity.JPAUtility;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

public class DAOFactory {
    public final PlayerDAO playerDAO;
    public final FarmDAO farmDAO;
    public final FleetDAO fleetDAO;
    public final GalaxyDAO galaxyDAO;
    public final TargetDAO targetDAO;
    public final ColonyDAO colonyDAO;
    public final MessageDAO messageDAO;
    public final CollectionDAO collectionDAO;
    public final ErrorDAO errorDAO;

    public DAOFactory() {
        playerDAO = new PlayerDAOImpl();
        farmDAO = new FarmDAOImpl();
        fleetDAO = new FleetDAOImpl();
        galaxyDAO = new GalaxyDAOImpl();
        targetDAO = new TargetDAOImpl();
        colonyDAO = new ColonyDAOImpl();
        messageDAO = new MessageDAOImpl();
        collectionDAO = new CollectionDAOImpl();
        errorDAO = new ErrorDAOImpl();
    }

}
