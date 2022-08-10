package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.UniverseEntity;

import javax.persistence.EntityManager;

public class ColonyDAOImpl extends AbstractDAOImpl<ColonyEntity> implements ColonyDAO {

    public ColonyDAOImpl(UniverseEntity universeEntity, EntityManager entityManager) {
        super(universeEntity, entityManager);
    }

    @Override
    protected Class<ColonyEntity> getEntitylass() {
        return ColonyEntity.class;
    }
}
