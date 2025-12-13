package org.enoch.snark.db.repository;

import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.types.ColonyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TargetRepository extends JpaRepository<TargetEntity, Long> {

    List<TargetEntity> findByGalaxyAndSystem(Integer galaxy, Integer system);
    Optional<TargetEntity> findByGalaxyAndSystemAndPositionAndType(Integer galaxy, Integer system, Integer position, ColonyType type);
    List<TargetEntity> findByGalaxy(Integer galaxy);

    @Query("""
    SELECT t
    FROM TargetEntity t
    JOIN t.player p
    WHERE p.type = 'IN_ACTIVE'
      AND t.type = 'PLANET'
      AND t.galaxy = :galaxy
      AND (
            t.fleetSum IS NULL
            OR t.defenseSum IS NULL
            OR (
                t.fleetSum = 0
                AND t.defenseSum = 0
                AND t.energy IS NOT NULL
                AND t.energy > 4000
            )
          )
    """)
    List<TargetEntity> farms(@Param("galaxy") Integer galaxy);

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
        return this.findByGalaxyAndSystemAndPositionAndType(planet.galaxy, planet.system, planet.position, planet.type);
    }

    default List<TargetEntity> findTargetsCloserTo(Planet source, List<Planet> others) {
         return farms(source.galaxy).stream()
                .filter(target -> isCloserToSourceThanOthers(target, source, others))
                .toList();
    }

    private boolean isCloserToSourceThanOthers(TargetEntity target, Planet source, List<Planet> others) {
        double distanceToSource = source.distance(target.toPlanet());
        return others.stream()
                .allMatch(other -> distanceToSource < other.distance(target.toPlanet()));
    }

}
