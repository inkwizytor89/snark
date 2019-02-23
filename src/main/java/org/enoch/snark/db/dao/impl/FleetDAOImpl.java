package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.UniverseEntity;

import javax.persistence.EntityTransaction;

public class FleetDAOImpl extends AbstractDAOImpl implements FleetDAO {

    FleetDAOImpl(UniverseEntity universeEntity) {
        super(universeEntity);
    }

    @Override
    public void saveOrUpdate(FleetEntity entity) {
        final EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        if(entity.id == null) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }
        transaction.commit();
    }
}
