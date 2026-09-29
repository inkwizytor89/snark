package org.enoch.snark.instance.service;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.instance.model.to.Planet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class PlanetCache {
    private static final Map<String, List<Planet>> cache = new HashMap<>();

    public static void put(String code, List<ColonyEntity> planets) {
        List<Planet> planetList = planets.stream()
                .filter(Objects::nonNull)
                .map(PlanetEntity::toPlanet)
                .collect(Collectors.toList());
        cache.put(code, planetList);
    }
}
