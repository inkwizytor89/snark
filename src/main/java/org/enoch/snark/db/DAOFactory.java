package org.enoch.snark.db;

import org.enoch.snark.db.dao.*;
import org.enoch.snark.db.dao.impl.*;
import org.enoch.snark.db.entity.JPAUtility;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

public class DAOFactory {
    public FarmDAO farmDAO;
    public FleetDAO fleetDAO;
    public GalaxyDAO galaxyDAO;
    public TargetDAO targetDAO;
    public ColonyDAO colonyDAO;
    public MessageDAO messageDAO;
    public CollectionDAO collectionDAO;
    public ErrorDAO errorDAO;

    public DAOFactory() {
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
