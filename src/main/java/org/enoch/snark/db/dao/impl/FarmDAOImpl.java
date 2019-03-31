package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.FarmDAO;
import org.enoch.snark.db.entity.FarmEntity;
import org.enoch.snark.db.entity.UniverseEntity;

import java.util.List;

public class FarmDAOImpl extends AbstractDAOImpl<FarmEntity> implements FarmDAO {

    public FarmDAOImpl(UniverseEntity universeEntity) {
        super(universeEntity);
    }

    @Override
    protected Class<FarmEntity> getEntitylass() {
        return FarmEntity.class;
    }

    @Override
    public FarmEntity getActualState() {
        List<FarmEntity> farmEntities = fetchAll();
        if(farmEntities.size() > 0) {
            return farmEntities.get(0);
        } else {
            return new FarmEntity();
        }
    }

    @Override
    public FarmEntity getPreviousState() {
        List<FarmEntity> farmEntities = fetchAll();
        if(farmEntities.size() > 0) {
            return farmEntities.get(0);
        } else {
            return new FarmEntity();
        }
    }
}
