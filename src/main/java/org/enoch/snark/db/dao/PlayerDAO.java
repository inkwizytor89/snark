package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.PlayerEntity;

import java.util.Optional;

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
    protected Class<PlayerEntity> getEntityClass() {
        return PlayerEntity.class;
    }

    public PlayerEntity find(String code) {
        synchronized (JPAUtility.dbSynchro) {
            Optional<PlayerEntity> playerOptional = entityManager.createQuery("" +
                    "from PlayerEntity " +
                    "where code = :code ", PlayerEntity.class)
                    .setParameter("code", code)
                    .getResultList().stream().findFirst();
            if(playerOptional.isPresent()) {
                return playerOptional.get();
            } else {
                PlayerEntity playerEntity = new PlayerEntity();
                playerEntity.code = code;
                return playerEntity;
            }
        }
    }
}
