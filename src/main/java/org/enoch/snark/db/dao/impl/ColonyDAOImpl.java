package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.UniverseEntity;

public class ColonyDAOImpl extends AbstractDAOImpl<ColonyEntity> implements ColonyDAO {

    public ColonyDAOImpl(UniverseEntity universeEntity) {
        super(universeEntity);
    }

    @Override
    protected Class<ColonyEntity> getEntitylass() {
        return ColonyEntity.class;
    }
}
