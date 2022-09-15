package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.db.entity.JPAUtility;

import java.util.List;

public class ColonyDAO extends AbstractDAO<ColonyEntity> {

    private static ColonyDAO INSTANCE;

    private ColonyDAO() {
        super();
    }

    public static ColonyDAO getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ColonyDAO();
        }
        return INSTANCE;
    }

    @Override
    protected Class<ColonyEntity> getEntityClass() {
        return ColonyEntity.class;
    }

    public ColonyEntity find(Integer cp) {
        synchronized (JPAUtility.dbSynchro) {
            List<ColonyEntity> resultList = entityManager.createQuery("" +
                    "from ColonyEntity " +
                    "where cp = :cp", ColonyEntity.class)
                    .setParameter("cp", cp)
                    .getResultList();
            if(resultList.isEmpty()) {
                return null;
            }
            return resultList.get(0);
        }
    }

    public ColonyEntity getOldestUpdated() {
        synchronized (JPAUtility.dbSynchro) {
            List<ColonyEntity> resultList = entityManager.createQuery(
                    "from ColonyEntity " +
                            "order by updated asc", ColonyEntity.class)
                    .getResultList();
            return resultList.stream().findFirst().get();
        }
    }
}
