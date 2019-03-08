package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.MessageDAO;
import org.enoch.snark.db.entity.MessageEntity;
import org.enoch.snark.db.entity.UniverseEntity;

public class MessageDAOImpl extends AbstractDAOImpl<MessageEntity> implements MessageDAO {

    public MessageDAOImpl(UniverseEntity universeEntity) {
        super(universeEntity);
    }

    @Override
    protected Class<MessageEntity> getEntitylass() {
        return MessageEntity.class;
    }
}
