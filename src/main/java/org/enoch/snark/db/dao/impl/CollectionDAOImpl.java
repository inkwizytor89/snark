package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.CollectionDAO;
import org.enoch.snark.db.dao.ErrorDAO;
import org.enoch.snark.db.entity.CollectionEntity;
import org.enoch.snark.db.entity.ErrorEntity;
import org.enoch.snark.db.entity.UniverseEntity;
import org.enoch.snark.exception.DatabseError;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CollectionDAOImpl extends AbstractDAOImpl<CollectionEntity> implements CollectionDAO {

    public CollectionDAOImpl(UniverseEntity universeEntity) {
        super(universeEntity);
    }

    @Override
    protected Class<CollectionEntity> getEntitylass() {
        return CollectionEntity.class;
    }

}
