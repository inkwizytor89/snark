package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.UniverseDAO;
import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.UniverseEntity;

import javax.persistence.EntityManager;
import java.util.List;

public class UniverseDAOImpl implements UniverseDAO {

    @Override
    public List<UniverseEntity> fetchAllUniverses() {
        EntityManager entityManager = JPAUtility.getEntityManager();
        return entityManager.createQuery("from UniverseEntity", UniverseEntity.class).getResultList();
    }
}
