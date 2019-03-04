package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.FleetRequestDAO;
import org.enoch.snark.db.entity.FleetRequestEntity;
import org.enoch.snark.db.entity.UniverseEntity;

public class FleetRequestDAOImpl extends AbstractDAOImpl<FleetRequestEntity> implements FleetRequestDAO {
    public FleetRequestDAOImpl(UniverseEntity universeEntity) {
        super(universeEntity);
    }

}
