package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.TargetEntity;

import java.util.List;
import java.util.Optional;

public interface TargetDAO extends AbstractDAO<TargetEntity> {

    List<TargetEntity> findFarms(Integer limit);

    Optional<TargetEntity> find(Integer galaxy, Integer system, Integer position);
}
