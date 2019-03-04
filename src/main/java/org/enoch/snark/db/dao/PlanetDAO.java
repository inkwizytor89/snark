package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.PlanetEntity;

import java.util.List;
import java.util.Optional;

public interface PlanetDAO  {

    void saveOrUpdate(PlanetEntity planet);

    List<PlanetEntity> findFarms(Integer limit);

    Optional<PlanetEntity> find(Integer galaxy, Integer system, Integer position);
}
