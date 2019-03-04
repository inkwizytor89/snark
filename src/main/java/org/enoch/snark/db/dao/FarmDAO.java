package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.FarmEntity;

public interface FarmDAO {
    void saveOrUpdate(FarmEntity farmEntity);
    FarmEntity getActualState();
    FarmEntity getPreviousState();
}
