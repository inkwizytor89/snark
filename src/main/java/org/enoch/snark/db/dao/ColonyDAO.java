package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.ColonyEntity;

public interface ColonyDAO extends AbstractDAO<ColonyEntity> {
    ColonyEntity find(Integer cp);
}
