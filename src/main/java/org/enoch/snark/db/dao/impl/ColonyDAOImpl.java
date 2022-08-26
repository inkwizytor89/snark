package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.UniverseEntity;

import javax.persistence.EntityManager;
import java.util.List;

public class ColonyDAOImpl extends AbstractDAOImpl<ColonyEntity> implements ColonyDAO {

    public ColonyDAOImpl(UniverseEntity universeEntity, EntityManager entityManager) {
        super(universeEntity, entityManager);
    }

    @Override
    protected Class<ColonyEntity> getEntitylass() {
        return ColonyEntity.class;
    }

    @Override
    public ColonyEntity find(Integer cp) {
        synchronized (JPAUtility.dbSynchro) {
            List<ColonyEntity> resultList = entityManager.createQuery("" +
                    "from ColonyEntity " +
                    "where  universe = :universe and " +
                    "       cp = :cp", ColonyEntity.class)
                    .setParameter("universe", universeEntity)
                    .setParameter("cp", cp)
                    .getResultList();
            if(resultList.isEmpty()) {
                return null;
            }
            return resultList.get(0);
        }
    }
}
