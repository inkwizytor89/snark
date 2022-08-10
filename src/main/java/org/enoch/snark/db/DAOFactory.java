package org.enoch.snark.db;

import org.enoch.snark.db.dao.*;
import org.enoch.snark.db.dao.impl.*;
import org.enoch.snark.db.entity.UniverseEntity;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

public class DAOFactory {
    public UniverseDAO universeDAO;
    public FarmDAO farmDAO;
    public FleetDAO fleetDAO;
    public GalaxyDAO galaxyDAO;
    public TargetDAO targetDAO;
    public ColonyDAO colonyDAO;
    public MessageDAO messageDAO;
    public CollectionDAO collectionDAO;
    public ErrorDAO errorDAO;
    public final EntityManager entityManager;

    public DAOFactory(UniverseDAOImpl universeDAO, @Nonnull UniverseEntity entity) {
        this.universeDAO = universeDAO;
        this.entityManager = universeDAO.entityManager;
        farmDAO = new FarmDAOImpl(entity, entityManager);
        fleetDAO = new FleetDAOImpl(entity, entityManager);
        galaxyDAO = new GalaxyDAOImpl(entity, entityManager);
        targetDAO = new TargetDAOImpl(entity, entityManager);
        colonyDAO = new ColonyDAOImpl(entity, entityManager);
        messageDAO = new MessageDAOImpl(entity, entityManager);
        collectionDAO = new CollectionDAOImpl(entity, entityManager);
        errorDAO = new ErrorDAOImpl(entity, entityManager);
    }

}
