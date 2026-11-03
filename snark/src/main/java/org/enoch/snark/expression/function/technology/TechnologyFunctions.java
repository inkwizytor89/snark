package org.enoch.snark.expression.function.technology;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.PlayerRepository;
import org.enoch.snark.instance.model.technology.Building;
import org.enoch.snark.instance.model.technology.LFBuilding;
import org.enoch.snark.instance.model.technology.Research;
import org.enoch.snark.instance.model.technology.Technology;
import org.enoch.snark.instance.model.to.Planet;

import java.math.BigDecimal;
import java.util.Map;

public final class TechnologyFunctions {

    private static ColonyRepository colonyRepository;
    private static PlayerRepository playerRepository;

    private TechnologyFunctions() {
    }

    public static void setRepositories(ColonyRepository colonyRepo, PlayerRepository playerRepo) {
        colonyRepository = colonyRepo;
        playerRepository = playerRepo;
    }

    public static boolean hasTechnologyGreaterThan(Map<String, Object> params) {
        TechnologyLevel technologyLevel = resolveTechnologyLevel(params);
        return technologyLevel.level().compareTo(technologyLevel.threshold()) > 0;
    }

    public static boolean hasTechnologyLessThan(Map<String, Object> params) {
        TechnologyLevel technologyLevel = resolveTechnologyLevel(params);
        return technologyLevel.level().compareTo(technologyLevel.threshold()) < 0;
    }

    private static TechnologyLevel resolveTechnologyLevel(Map<String, Object> params) {
        if (params == null) {
            throw new IllegalArgumentException("Technology parameters cannot be null");
        }

        String name = requiredValue(params, "name");
        BigDecimal threshold = parseLevel(requiredValue(params, "level"));
        Technology technology = findTechnology(name);

        if (technology instanceof Research research) {
            PlayerEntity player = playerRepository.mainPlayer();
            return new TechnologyLevel(BigDecimal.valueOf(player.getResearchLevel(research)), threshold);
        }

        String coordinate = requiredValue(params, "coordinate");
        Planet planet = Planet.parse(coordinate);
        if (planet == null) {
            throw new IllegalArgumentException("Coordinate cannot be null or blank");
        }
        ColonyEntity colony = colonyRepository.byPlanet(planet);
        return new TechnologyLevel(BigDecimal.valueOf(colony.getBuildingLevel(technology)), threshold);
    }

    private static Technology findTechnology(String name) {
        try {
            return Research.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            try {
                return Building.valueOf(name);
            } catch (IllegalArgumentException ignoredBuilding) {
                try {
                    return LFBuilding.valueOf(name);
                } catch (IllegalArgumentException ignoredLifeformBuilding) {
                    throw new IllegalArgumentException("Unknown technology name: " + name);
                }
            }
        }
    }

    private static String requiredValue(Map<String, Object> params, String name) {
        Object value = params.get(name);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Required technology parameter missing: " + name);
        }
        return value.toString();
    }

    private static BigDecimal parseLevel(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Technology level must be numeric: " + value, exception);
        }
    }

    private record TechnologyLevel(BigDecimal level, BigDecimal threshold) {
    }
}
