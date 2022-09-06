package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.CollectionEntity;

public class CollectionDAO extends AbstractDAO<CollectionEntity> {
    private static CollectionDAO INSTANCE;

    private CollectionDAO() {
        super();
    }

    public static CollectionDAO getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new CollectionDAO();
        }
        return INSTANCE;
    }

    @Override
    protected Class<CollectionEntity> getEntitylass() {
        return CollectionEntity.class;
    }

}
