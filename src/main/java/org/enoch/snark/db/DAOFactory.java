package org.enoch.snark.db;

import org.enoch.snark.db.dao.*;
import org.enoch.snark.db.dao.impl.*;
import org.enoch.snark.db.entity.JPAUtility;
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
    public final UniverseEntity universeEntity;

    public DAOFactory(@Nonnull Long universeId) {
        this.entityManager = JPAUtility.getEntityManager();
        universeDAO = new UniverseDAOImpl(entityManager);
        universeEntity = entityManager.find(UniverseEntity.class, universeId);
        farmDAO = new FarmDAOImpl(universeEntity, entityManager);
        fleetDAO = new FleetDAOImpl(universeEntity, entityManager);
        galaxyDAO = new GalaxyDAOImpl(universeEntity, entityManager);
        targetDAO = new TargetDAOImpl(universeEntity, entityManager);
        colonyDAO = new ColonyDAOImpl(universeEntity, entityManager);
        messageDAO = new MessageDAOImpl(universeEntity, entityManager);
        collectionDAO = new CollectionDAOImpl(universeEntity, entityManager);
        errorDAO = new ErrorDAOImpl(universeEntity, entityManager);
    }

}
