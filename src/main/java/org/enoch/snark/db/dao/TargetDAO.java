package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.TargetEntity;

import java.util.List;
import java.util.Optional;

public interface TargetDAO extends AbstractDAO<TargetEntity> {

    Optional<TargetEntity> find(Integer galaxy, Integer system, Integer position);

    List<TargetEntity> findFarms(Integer limit);

    List<TargetEntity> findTopFarms(int limit);
}
