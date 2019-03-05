package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.BaseEntity;

import javax.annotation.Nonnull;
import java.util.List;

public interface AbstractDAO<T extends BaseEntity> {

    @Nonnull
    T saveOrUpdate(@Nonnull T entity);

    @Nonnull
    List<T> fetchAll();
}
