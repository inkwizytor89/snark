package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.UniverseEntity;

public class FleetDAOImpl extends AbstractDAOImpl<FleetEntity> implements FleetDAO {

    public FleetDAOImpl(UniverseEntity universeEntity) {
        super(universeEntity);
    }
}
