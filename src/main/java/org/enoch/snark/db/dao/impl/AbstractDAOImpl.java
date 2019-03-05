package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.entity.BaseEntity;
import org.enoch.snark.db.entity.IdEntity;
import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.UniverseEntity;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

public abstract class AbstractDAOImpl<T extends BaseEntity> {

    protected final EntityManager entityManager;
    protected UniverseEntity universeEntity;

    AbstractDAOImpl(UniverseEntity universeEntity) {
        this.universeEntity = universeEntity;
        entityManager = JPAUtility.getEntityManager();
    }

    public void saveOrUpdate(@Nonnull T entity) {
        final EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        if(entity.id == null) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }
        transaction.commit();
    }

    public List fetchAll(Class clazz) {
        return entityManager.createQuery("from " + clazz.getName())
                .getResultList();

    }
}
