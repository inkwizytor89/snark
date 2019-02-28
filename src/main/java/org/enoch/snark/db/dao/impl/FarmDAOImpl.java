package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.FarmDAO;
import org.enoch.snark.db.entity.FarmEntity;
import org.enoch.snark.db.entity.UniverseEntity;

import javax.persistence.EntityTransaction;
import java.util.List;

public class FarmDAOImpl extends AbstractDAOImpl implements FarmDAO {

    public FarmDAOImpl(UniverseEntity universeEntity) {
        super(universeEntity);
    }

    @Override
    public FarmEntity getActualState() {
        List<FarmEntity> farmEntities = fetchAll();
        if(farmEntities.size() > 0) {
            return farmEntities.get(0);
        } else {
            return new FarmEntity();
        }
    }

    @Override
    public FarmEntity getPreviousState() {
        List<FarmEntity> farmEntities = fetchAll();
        if(farmEntities.size() > 0) {
            return farmEntities.get(0);
        } else {
            return new FarmEntity();
        }
    }

    private List<FarmEntity> fetchAll() {
        return entityManager.createQuery(
                "from FarmEntity " +
                        "order by start desc", FarmEntity.class
        ).getResultList();
    }

    @Override
    public void saveOrUpdate(FarmEntity farmEntity) {
        final EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        if(farmEntity.id == null) {
            entityManager.persist(farmEntity);
        } else {
            entityManager.merge(farmEntity);
        }
        transaction.commit();
    }
}
