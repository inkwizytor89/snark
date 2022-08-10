package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.UniverseDAO;
import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.UniverseEntity;

import javax.persistence.EntityManager;
import java.util.List;

public class UniverseDAOImpl implements UniverseDAO {

    public EntityManager entityManager;

    public UniverseDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public synchronized List<UniverseEntity> fetchAllUniverses() {
        return entityManager.createQuery("from UniverseEntity", UniverseEntity.class).getResultList();
    }
}
