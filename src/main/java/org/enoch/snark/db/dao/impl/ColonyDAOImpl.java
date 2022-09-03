package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.JPAUtility;

import java.util.List;

public class ColonyDAOImpl extends AbstractDAOImpl<ColonyEntity> implements ColonyDAO {

    public ColonyDAOImpl() {
        super();
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
                    "where cp = :cp", ColonyEntity.class)
                    .setParameter("cp", cp)
                    .getResultList();
            if(resultList.isEmpty()) {
                return null;
            }
            return resultList.get(0);
        }
    }
}
