package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.FarmDAO;
import org.enoch.snark.db.entity.FarmEntity;
import org.enoch.snark.db.entity.UniverseEntity;

public class FarmDAOImpl extends AbstractDAOImpl implements FarmDAO {

    public FarmDAOImpl(UniverseEntity universeEntity) {
        super(universeEntity);
    }

    @Override
    public FarmEntity getActualState() {
        return null;
    }
}
