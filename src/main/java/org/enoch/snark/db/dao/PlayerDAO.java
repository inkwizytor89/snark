package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.PlayerEntity;

public class PlayerDAO extends AbstractDAO<PlayerEntity> {

    private static PlayerDAO INSTANCE;

    private PlayerDAO() {
        super();
    }

    public static PlayerDAO getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new PlayerDAO();
        }
        return INSTANCE;
    }

    @Override
    protected Class<PlayerEntity> getEntitylass() {
        return PlayerEntity.class;
    }
}
