package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.AbstractDAO;
import org.enoch.snark.db.entity.BaseEntity;
import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.UniverseEntity;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

public abstract class AbstractDAOImpl<T extends BaseEntity> implements AbstractDAO<T> {

    final EntityManager entityManager;
    protected UniverseEntity universeEntity;

    AbstractDAOImpl(UniverseEntity universeEntity, EntityManager entityManager) {
        this.universeEntity = universeEntity;
        this.entityManager = entityManager;
    }

    protected abstract Class<T> getEntitylass();

    @Nonnull
    public T saveOrUpdate(@Nonnull final T entity) {
        T savedEntity = entity;
        synchronized (getEntitylass()) {
            final EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            if(entity.id == null) {
                entityManager.persist(savedEntity);
            } else {
                savedEntity = entityManager.merge(savedEntity);
            }
            transaction.commit();
        }
        return savedEntity;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public List<T> fetchAll() {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("from " + getEntitylass().getSimpleName())
                    .getResultList();
        }
    }

    @Nonnull
    public T fetch(T entity) {
        synchronized (JPAUtility.dbSynchro) {
            T t = entityManager.find(getEntitylass(), entity.id);
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
}
