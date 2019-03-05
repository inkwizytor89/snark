package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.FarmEntity;

public interface FarmDAO extends AbstractDAO<FarmEntity> {
    FarmEntity getActualState();
    FarmEntity getPreviousState();
}
