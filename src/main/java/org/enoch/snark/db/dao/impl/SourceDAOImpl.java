package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.SourceDAO;
import org.enoch.snark.db.entity.SourceEntity;
import org.enoch.snark.db.entity.UniverseEntity;

import java.util.List;

public class SourceDAOImpl extends AbstractDAOImpl<SourceEntity> implements SourceDAO {

    public SourceDAOImpl(UniverseEntity universeEntity) {
        super(universeEntity);
    }

    @Override
    public List<SourceEntity> fetchAll() {
        return fetchAll(SourceEntity.class);
    }
}
