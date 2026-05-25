package org.enoch.snark.expression.function;

import org.enoch.snark.expression.definition.SpelFunctionDefinition;
import org.enoch.snark.expression.definition.SpellType;
import org.enoch.snark.instance.model.Coordinate;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.service.PlanetDataService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CoordinateMap extends AbstractSpelFunction {

    public static final String FUNCTION_NAME = "CoordinateMap";
    private static final String FUNCTION_DESCRIPTION = "Change coordinate to required way: ";

    public CoordinateMap() {
        register(this);
    }

    @Override
    public SpelFunctionDefinition getDefinition() {
        return new SpelFunctionDefinition(
                FUNCTION_NAME,
                FUNCTION_DESCRIPTION,
                List.of(),  // No parameters required
                SpellType.COORDINATES
        );
    }

    @Override
    public Object execute(Map<String, Object> args) {
        throw new UnsupportedOperationException("Use the static map method with parameters instead");
    }

    public static  List<Coordinate> map(List<Coordinate> coordinateList, String type) {
        return switch (type) {
            case "swap" -> coordinateList.stream()
                    .map(Coordinate::toCoordinate)
                    .map(Planet::swapType)
                    .collect(Collectors.toUnmodifiableList());
            case "space" -> coordinateList.stream()
                    .map(Coordinate::toCoordinate)
                    .map(Planet::toSpace)
                    .collect(Collectors.toUnmodifiableList());
            case "right_system" -> coordinateList.stream()
                    .map(Coordinate::toCoordinate)
                    .map(planet -> {
                        Planet changedPlanet = new Planet(planet.toString());
                        changedPlanet.system = changedPlanet.system + 1;
                        return changedPlanet;
                    })
                    .collect(Collectors.toUnmodifiableList());
            case "left_system" -> coordinateList.stream()
                    .map(Coordinate::toCoordinate)
                    .map(planet -> {
                        Planet changedPlanet = new Planet(planet.toString());
                        changedPlanet.system = changedPlanet.system - 1;
                        return changedPlanet;
                    })
                    .collect(Collectors.toUnmodifiableList());
            case "to_planet" -> coordinateList.stream()
                    .map(Coordinate::toCoordinate)
                    .peek(planet -> planet.type = ColonyType.PLANET)
                    .collect(Collectors.toUnmodifiableList());
            case "to_moon" -> coordinateList.stream()
                    .map(Coordinate::toCoordinate)
                    .peek(planet -> planet.type = ColonyType.MOON)
                    .collect(Collectors.toUnmodifiableList());
            default -> throw new IllegalArgumentException("Unsupported coordinate map type: " + type);
        };
    }
}