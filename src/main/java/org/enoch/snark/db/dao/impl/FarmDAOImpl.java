package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.FarmDAO;
import org.enoch.snark.db.entity.FarmEntity;
import org.enoch.snark.db.entity.JPAUtility;

import java.util.List;

public class FarmDAOImpl extends AbstractDAOImpl<FarmEntity> implements FarmDAO {

    public FarmDAOImpl() {
        super();
    }

    @Override
    protected Class<FarmEntity> getEntitylass() {
        return FarmEntity.class;
    }

    @Override
    public FarmEntity getActualState() {
        List<FarmEntity> farmEntities = getSorted();
        if(farmEntities.size() > 0) {
            return farmEntities.get(0);
        } else {
            return null;

        }
    }

    @Override
    public FarmEntity getPreviousState() {
        List<FarmEntity> farmEntities = getSorted();
        if(farmEntities.size() > 0) {
            return farmEntities.get(1);
        } else {
            return null;
        }
    }

    public List<FarmEntity> getSorted() {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("" +
                    "from FarmEntity order by start desc", FarmEntity.class)
                    .getResultList();
        }
    }
}
