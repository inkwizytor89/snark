package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.db.entity.PlayerEntity;

public class PlayerDAOImpl extends AbstractDAOImpl<PlayerEntity> implements PlayerDAO {

    @Override
    protected Class<PlayerEntity> getEntitylass() {
        return PlayerEntity.class;
    }
}
