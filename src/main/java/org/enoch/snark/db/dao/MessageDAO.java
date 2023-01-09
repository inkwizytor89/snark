package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.MessageEntity;

import java.time.LocalDateTime;

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

    public void clean(LocalDateTime from) {
        synchronized (JPAUtility.dbSynchro) {
            entityManager.createQuery("" +
                    "delete from MessageEntity " +
                    "where created < :from ", MessageEntity.class)
                    .setParameter("from", from)
                    .executeUpdate();
        }
    }
}
