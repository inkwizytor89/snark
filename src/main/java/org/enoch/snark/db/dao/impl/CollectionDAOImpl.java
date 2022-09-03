package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.CollectionDAO;
import org.enoch.snark.db.entity.CollectionEntity;

import javax.persistence.EntityManager;

public class CollectionDAOImpl extends AbstractDAOImpl<CollectionEntity> implements CollectionDAO {

    public CollectionDAOImpl() {
        super();
    }

    @Override
    protected Class<CollectionEntity> getEntitylass() {
        return CollectionEntity.class;
    }

}
