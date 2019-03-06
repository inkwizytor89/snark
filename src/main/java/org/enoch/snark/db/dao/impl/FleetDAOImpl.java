package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.UniverseEntity;

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
        return entityManager.createQuery("" +
                "select max(e.code) from FleetEntity e",
                Long.class)
                .getSingleResult() +1;
    }

    @Override
    public List<FleetEntity> findWithCode(Long code) {
        return entityManager.createQuery("" +
                "from FleetEntity " +
                "where code = :code",
                FleetEntity.class)
                .setParameter("code", code)
                .getResultList();
    }
}
