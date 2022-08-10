package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.MessageDAO;
import org.enoch.snark.db.entity.MessageEntity;
import org.enoch.snark.db.entity.UniverseEntity;

import javax.persistence.EntityManager;

public class MessageDAOImpl extends AbstractDAOImpl<MessageEntity> implements MessageDAO {

    public MessageDAOImpl(UniverseEntity universeEntity, EntityManager entityManager) {
        super(universeEntity, entityManager);
    }

    @Override
    protected Class<MessageEntity> getEntitylass() {
        return MessageEntity.class;
    }
}
