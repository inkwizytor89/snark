package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.JPAUtility;

import java.time.LocalDateTime;
import java.util.List;

public class FleetDAO extends AbstractDAO<FleetEntity> {

    private static FleetDAO INSTANCE;

    private FleetDAO() {
        super();
    }

    public static FleetDAO getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new FleetDAO();
        }
        return INSTANCE;
    }

    @Override
    protected Class<FleetEntity> getEntityClass() {
        return FleetEntity.class;
    }

    public Long genereteNewCode() {
        synchronized (JPAUtility.dbSynchro) {
            Long singleResult = entityManager.createQuery("" +
                    "select max(e.code) from FleetEntity e", Long.class)
                    .getSingleResult();
            if(singleResult == null) {
                singleResult = 1L;
            }
            return singleResult + 1;
        }
    }

    public List<FleetEntity> findWithCode(Long code) {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("" +
                    "from FleetEntity " +
                    "where code = :code", FleetEntity.class)
                    .setParameter("code", code)
                    .getResultList();
        }
    }

    public List<FleetEntity> findToProcess() {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("" +
                    "from FleetEntity " +
                    "where start is null ", FleetEntity.class)
                    .getResultList();
        }
    }
}
