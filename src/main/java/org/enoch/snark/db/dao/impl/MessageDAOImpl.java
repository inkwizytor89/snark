package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.MessageDAO;
import org.enoch.snark.db.entity.MessageEntity;

public class MessageDAOImpl extends AbstractDAOImpl<MessageEntity> implements MessageDAO {

    public MessageDAOImpl() {
        super();
    }

    @Override
    protected Class<MessageEntity> getEntitylass() {
        return MessageEntity.class;
    }
}
