package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.FleetEntity;

public interface FleetDAO {

    void saveOrUpdate(FleetEntity entity);
}