package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.MessageEntity;

import javax.persistence.EntityTransaction;
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
            transaction.begin();
            int update = entityManager.createQuery("" +
                    "delete from MessageEntity " +
                    "where created < :from ")
                    .setParameter("from", from)
                    .executeUpdate();

            System.err.println("org.enoch.snark.db.dao.MessageDAO.clean "+update);
            JPAUtility.syncMethod = null;
            entityManager.flush();
            transaction.commit();
        }
    }
}
