package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.MessageEntity;

public class MessageDAO extends AbstractDAO<MessageEntity> {

    private static MessageDAO INSTANCE;

    private MessageDAO() {
        super();
    }

    public static MessageDAO getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new MessageDAO();
        }
        return INSTANCE;
    }

    @Override
    protected Class<MessageEntity> getEntityClass() {
        return MessageEntity.class;
    }
}
