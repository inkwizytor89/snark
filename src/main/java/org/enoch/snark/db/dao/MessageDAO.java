package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.MessageEntity;

import jakarta.persistence.EntityTransaction;
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

            if(JPAUtility.syncMethod != null) System.err.println("Error: synchronization collision with "+JPAUtility.syncMethod);
            JPAUtility.syncMethod = "org.enoch.snark.db.dao.MessageDAO.clean";

            final EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                entityManager.createQuery("" +
                        "delete from MessageEntity " +
                        "where created < :from ")
                        .setParameter("from", from)
                        .executeUpdate();

                JPAUtility.syncMethod = null;
                entityManager.flush();
                transaction.commit();
            } catch (Exception e) {
                System.err.println("transaction MessageDAO.clean error "+e.getMessage());
                e.printStackTrace();
            } finally {
                if(transaction.isActive()) transaction.rollback();
            }
        }
    }
}
