package org.enoch.snark.db.repository;

import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.model.to.Planet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TargetRepository extends JpaRepository<TargetEntity, Long> {

    default TargetEntity byPlanet(Planet planet) {
        Optional<TargetEntity> first = findAll().stream()
                .filter(colony -> planet.galaxy.equals(colony.galaxy))
                .filter(colony -> planet.system.equals(colony.system))
                .filter(colony -> planet.position.equals(colony.position))
                .filter(colony -> planet.type.equals(colony.type))
                .findFirst();
        return first.orElseThrow(() -> new RuntimeException(planet + " not known as target"));
    }
}
