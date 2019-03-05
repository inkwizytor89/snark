package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.TargetEntity;

import java.util.List;
import java.util.Optional;

public interface PlanetDAO  {

    void saveOrUpdate(TargetEntity planet);

    List<TargetEntity> findFarms(Integer limit);

    Optional<TargetEntity> find(Integer galaxy, Integer system, Integer position);
}
