package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.AbstractDAO;
import org.enoch.snark.db.entity.BaseEntity;
import org.enoch.snark.db.entity.IdEntity;
import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.UniverseEntity;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

public abstract class AbstractDAOImpl<T extends BaseEntity> implements AbstractDAO<T> {

    protected final EntityManager entityManager;
    protected UniverseEntity universeEntity;

    AbstractDAOImpl(UniverseEntity universeEntity) {
        this.universeEntity = universeEntity;
        entityManager = JPAUtility.getEntityManager();
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
        return entityManager.createQuery("from " + getEntitylass().getName())
                .getResultList();

    }
}
