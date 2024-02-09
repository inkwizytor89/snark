package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.IdEntity;
import org.enoch.snark.db.entity.JPAUtility;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

public abstract class AbstractDAO<T extends IdEntity> {

    final EntityManager entityManager;

    AbstractDAO() {
        this.entityManager = JPAUtility.getEntityManager();
    }

    protected abstract Class<T> getEntityClass();

    @Nonnull
    public T saveOrUpdate(@Nonnull final T entity) {
        T savedEntity = entity;
        synchronized (JPAUtility.dbSynchro) {

            if(JPAUtility.syncMethod != null) System.err.println("Error: synchronization collision with "+JPAUtility.syncMethod);
            JPAUtility.syncMethod = "org.enoch.snark.db.dao.AbstractDAO.saveOrUpdate "+getEntityClass().getName();

            final EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            if(entity.id == null) {
                entityManager.persist(savedEntity);
            } else {
                savedEntity = entityManager.merge(savedEntity);
            }
            transaction.commit();

            JPAUtility.syncMethod=null;
        }
        return savedEntity;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public List<T> fetchAll() {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("from " + getEntityClass().getSimpleName())
                    .getResultList();
        }
    }

    @Nonnull
    public T fetch(T entity) {
        synchronized (JPAUtility.dbSynchro) {
            T t = entityManager.find(getEntityClass(), entity.id);
            if (t == null) {
                return entity;
            }
            return t;
        }
    }

    public void remove(T entity) {
        synchronized (JPAUtility.dbSynchro) {
            final EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.remove(entity);
            entityManager.flush();
            transaction.commit();
        }
    }

    public void refresh(T entity) {
        synchronized (JPAUtility.dbSynchro) {
            final EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                entityManager.refresh(entity);
                transaction.commit();
            } catch (Exception e) {
                System.err.println("transaction AbstractDAO.refresh error " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (transaction.isActive()) transaction.rollback();
            }
        }
    }
}