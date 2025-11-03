package org.enoch.snark.db.repository;

import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.model.to.Planet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TargetRepository extends JpaRepository<TargetEntity, Long> {

    List<TargetEntity> findByGalaxyAndSystem(Integer galaxy, Integer system);

    default TargetEntity byPlanet(Planet planet) {
        Optional<TargetEntity> first = findAll().stream()
                .filter(colony -> planet.galaxy.equals(colony.galaxy))
                .filter(colony -> planet.system.equals(colony.system))
                .filter(colony -> planet.position.equals(colony.position))
                .filter(colony -> planet.type.equals(colony.type))
                .findFirst();
        return first.orElseThrow(() -> new RuntimeException(planet + " not known as target"));
    }

    default Optional<TargetEntity> find(Planet planet) {
        return findAll().stream()
                .filter(colony -> planet.galaxy.equals(colony.galaxy))
                .filter(colony -> planet.system.equals(colony.system))
                .filter(colony -> planet.position.equals(colony.position))
                .filter(colony -> planet.type.equals(colony.type))
                .findFirst();
    }
}
