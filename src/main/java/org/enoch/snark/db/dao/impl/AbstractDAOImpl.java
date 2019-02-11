package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.UniverseEntity;

import javax.persistence.EntityManager;

public abstract class AbstractDAOImpl {

    protected final EntityManager entityManager;
    protected UniverseEntity universeEntity;

//    AbstractDAOImpl() {
//        entityManager = JPAUtility.getEntityManager();
//    }

    AbstractDAOImpl(UniverseEntity universeEntity) {
        this.universeEntity = universeEntity;
        entityManager = JPAUtility.getEntityManager();
    }
}
