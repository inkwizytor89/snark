package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.FleetEntity;

import java.util.List;

public interface FleetDAO extends AbstractDAO<FleetEntity> {

    Long genereteNewCode();

    List<FleetEntity> findWithCode(Long code);

    List<FleetEntity> findToProcess();
}
