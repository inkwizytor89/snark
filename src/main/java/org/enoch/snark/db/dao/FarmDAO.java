package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.FarmEntity;
import org.enoch.snark.db.entity.JPAUtility;

import java.util.List;

public class FarmDAO extends AbstractDAO<FarmEntity> {

    private static FarmDAO INSTANCE;

    private FarmDAO() {
        super();
    }

    public static FarmDAO getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new FarmDAO();
        }
        return INSTANCE;
    }

    @Override
    protected Class<FarmEntity> getEntitylass() {
        return FarmEntity.class;
    }

    public FarmEntity getActualState() {
        List<FarmEntity> farmEntities = getSorted();
        if(farmEntities.size() > 0) {
            return farmEntities.get(0);
        } else {
            return null;

        }
    }

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
