package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.UniverseEntity;

import java.time.LocalDateTime;
import java.util.List;

public class FleetDAOImpl extends AbstractDAOImpl<FleetEntity> implements FleetDAO {

    public FleetDAOImpl(UniverseEntity universeEntity) {
        super(universeEntity);
    }

    @Override
    protected Class<FleetEntity> getEntitylass() {
        return FleetEntity.class;
    }

    @Override
    public Long genereteNewCode() {
        synchronized (JPAUtility.dbSynchro) {
            Long singleResult = entityManager.createQuery("" +
                    "select max(e.code) from FleetEntity e", Long.class)
                    .getSingleResult();
            if(singleResult == null) {
                singleResult = 0L;
            }
            return singleResult + 1;
        }
    }

    @Override
    public List<FleetEntity> findWithCode(Long code) {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("" +
                    "from FleetEntity " +
                    "where  universe = :universe and " +
                    "       code = :code", FleetEntity.class)
                    .setParameter("universe", universeEntity)
                    .setParameter("code", code)
                    .getResultList();
        }
    }

    @Override
    public List<FleetEntity> findToProcess() {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("" +
                    "from FleetEntity " +
                    "where  universe = :universe and " +
                    "       start is not null and " +
                    "       start < :now and " +
                    "       visited is null", FleetEntity.class)
                    .setParameter("universe", universeEntity)
                    .setParameter("now", LocalDateTime.now())
                    .getResultList();
        }
    }
}
