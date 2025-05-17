package org.enoch.snark.db.repository;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.service.PlanetCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.model.action.PlanetExpression.MOON;
import static org.enoch.snark.instance.model.action.PlanetExpression.PLANET;

@Repository
public interface ColonyRepository extends JpaRepository<ColonyEntity, Long> {

    Optional<ColonyEntity> findByCp(Integer cp);

    default List<ColonyEntity> findByCode(String code) {
        String lowerCode = code.toLowerCase();
        List<ColonyEntity> colonies;
        if(lowerCode.contains(MOON)) return moons();
        else if(lowerCode.contains(PLANET)) return planets();
        else if(lowerCode.equals(StringUtils.EMPTY)) return allPositions();
        else return coloniesList(lowerCode);
    }

    default List<ColonyEntity> moons() {
        return findAll()
                .stream()
                .filter(colonyEntity -> !colonyEntity.is(ColonyType.PLANET))
                .sorted(Comparator.comparing(o -> -o.galaxy))
                .collect(Collectors.toList());
    }

    default List<ColonyEntity> planets() {
        return findAll()
                .stream()
                .filter(colonyEntity -> colonyEntity.is(ColonyType.PLANET))
                .sorted(Comparator.comparing(o -> -o.galaxy))
                .collect(Collectors.toList());
    }

    default List<ColonyEntity> allPositions() {
        List<ColonyEntity> colonies = new ArrayList<>();
        for (ColonyEntity planet : planets()) {
            ColonyEntity colony = planet;
            if (planet.cpm != null) colony = findByCp(planet.cpm).get();
            colonies.add(colony);
        }
        return colonies;
    }

    default List<ColonyEntity> coloniesList(String coloniesList) {
        return Planet.fromString(coloniesList).stream()
                .map(this::byPlanet)
                .collect(Collectors.toList());
    }

    default ColonyEntity byPlanet(Planet planet) {
        Optional<ColonyEntity> first = findAll().stream()
                .filter(colony -> planet.galaxy.equals(colony.galaxy))
                .filter(colony -> planet.system.equals(colony.system))
                .filter(colony -> planet.position.equals(colony.position))
                .filter(colony -> planet.type.equals(colony.type))
                .findFirst();
        return first.orElseThrow(() -> new RuntimeException(planet+" not known"));
    }
}
