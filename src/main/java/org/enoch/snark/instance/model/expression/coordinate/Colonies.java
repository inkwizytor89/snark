package org.enoch.snark.instance.model.expression.coordinate;

import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.PlanetData;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Swap first two elements operation: [A,B,C] -> [B,A,C]
 */
public class Colonies {

    private static final String PLANETS = "PLANETS";
    private static final String MOONS = "MOONS";
    private static final String ALL = "ALL";
    private static final String EACH_POSITION = EMPTY;
    private static final String NONE = "NONE";

    private static ColonyRepository colonyRepository;

    public static synchronized void setRepository(ColonyRepository colonyRepo) {
        colonyRepository = colonyRepo;
    }

    public static Object planets() {
        return colonyRepository.findByCode(PLANETS).stream()
                .map(PlanetEntity::toString)
                .toList();
    }
    public static Object moons() {
        return colonyRepository.findByCode(MOONS).stream()
                .map(PlanetEntity::toString)
                .toList();
    }

    public static Object all() {
        return colonyRepository.findByCode(ALL).stream()
                .map(PlanetEntity::toString)
                .toList();
    }

    public static Object allPositions() {
        return colonyRepository.findByCode(EACH_POSITION).stream()
                .map(PlanetEntity::toString)
                .toList();
    }

    public static Object none() {
        return new ArrayList<>();
    }
}

