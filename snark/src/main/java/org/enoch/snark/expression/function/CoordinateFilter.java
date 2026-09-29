package org.enoch.snark.expression.function;

import org.enoch.snark.expression.definition.SpelFunctionDefinition;
import org.enoch.snark.expression.definition.SpellType;
import org.enoch.snark.instance.model.Coordinate;
import org.enoch.snark.instance.model.to.PlanetData;
import org.enoch.snark.instance.service.PlanetDataService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Function that returns all moons from the colony repository
 * Returns: COORDINATES type - List<PlanetData>
 */
public class CoordinateFilter extends AbstractSpelFunction {

    public static final String FUNCTION_NAME = "coordinate_filter";
    private static final String FUNCTION_DESCRIPTION = "Returns coordinate filtered by its name and value. Supported filters: type, galaxy, system, position";

    private static PlanetDataService planetDataService;

    public CoordinateFilter() {
        register(this);
    }

    public static void setRepository(PlanetDataService planetDataService) {
        CoordinateFilter.planetDataService = planetDataService;
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

    public static  List<Coordinate> filter(Map<String, String> filters) {
        Stream<PlanetData> stream = planetDataService.allColonies().stream();
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String filterKey = entry.getKey();
            String filterValue = entry.getValue();
            switch (filterKey.toLowerCase()) {
                case "type" -> stream = stream.filter(planetData -> planetData.getPlanet().type.name().equalsIgnoreCase(filterValue));
                case "galaxy" -> stream = stream.filter(planetData -> planetData.getPlanet().galaxy == Integer.parseInt(filterValue));
                case "system" -> stream = stream.filter(planetData -> planetData.getPlanet().system == Integer.parseInt(filterValue));
                case "position" -> stream = stream.filter(planetData -> planetData.getPlanet().position == Integer.parseInt(filterValue));
                // Add more filters as needed
            }
        }
        return stream.collect(Collectors.toUnmodifiableList());
    }
}