package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.PlanetEntity;

import java.util.Optional;

public interface PlanetDAO {

    void saveOrUpdatePlanet(PlanetEntity planet);

    Optional<PlanetEntity> find(Integer galaxy, Integer system, Integer position);
}
