package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.IdEntity;
import org.enoch.snark.db.entity.JPAUtility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.List;

public abstract class AbstractDAO<T extends IdEntity> {

    final EntityManager entityManager;

    AbstractDAO() {
        this.entityManager = JPAUtility.getEntityManager();
    }

    protected abstract Class<T> getEntityClass();

    public T saveOrUpdate(final T entity) {
        T savedEntity = entity;
        synchronized (JPAUtility.dbSynchro) {

            if(JPAUtility.syncMethod != null) System.err.println("Error: synchronization collision with "+JPAUtility.syncMethod);
            JPAUtility.syncMethod = "org.enoch.snark.db.dao.AbstractDAO.saveOrUpdate "+getEntityClass().getName();

            final EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entity.updated = LocalDateTime.now();
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

    @SuppressWarnings("unchecked")
    public List<T> fetchAll() {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("from " + getEntityClass().getSimpleName())
                    .getResultList();
        }
    }

    public T fetch(T entity) {
        synchronized (JPAUtility.dbSynchro) {
            T t = entityManager.find(getEntityClass(), entity.id);
            if (t == null) {
                return entity;
            }
            return t;
        }
    }

    public List<T> fetchAll(List<T> list) {
        return list.stream().map(this::fetch).toList();
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
}