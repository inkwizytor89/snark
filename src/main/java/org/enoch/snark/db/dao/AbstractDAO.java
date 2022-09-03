package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.IdEntity;

import javax.annotation.Nonnull;
import java.util.List;

public interface AbstractDAO<T extends IdEntity> {

    @Nonnull
    T saveOrUpdate(@Nonnull T entity);

    @Nonnull
    List<T> fetchAll();

    @Nonnull
    T fetch(T entity);

    void remove(T entity);
}
