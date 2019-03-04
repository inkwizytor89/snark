package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.FleetRequestEntity;

public interface FleetRequestDAO {

    void saveOrUpdate(FleetRequestEntity fleetRequestEntity);

}
