package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.FarmEntity;

public interface FarmDAO {

    FarmEntity getActualState();
    FarmEntity getPreviousState();
    void saveOrUpdate(FarmEntity farmEntity);
}
