package org.enoch.snark.expression.coordinate.function;

import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.PlanetData;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.service.PlanetDataService;

import java.util.List;

/**
 * Swap first two elements operation: [A,B,C] -> [B,A,C]
 */
public class Swarn {

    private static PlanetDataService planetDataService;

    public static void setRepository(PlanetDataService planetDataService) {
        Swarn.planetDataService = planetDataService;
    }

    public static List<PlanetData> farm(List<PlanetData> source) {
        return farm(source.getFirst().getPlanet().toString());
    }

    public static List<PlanetData> farm(String sourceString) {
        List<Planet> sources = Planet.fromString(sourceString);
        if(sources.size() > 1) {
            throw new IllegalArgumentException("farm operation requires exactly one source planet: "+sourceString);
        }
        Planet source = sources.getFirst();
        source = source.is(ColonyType.MOON) ? source.swapType() : source;
        Planet finalSource = source;
        List<Planet> others = planetDataService.allColonies().stream()
                .map(PlanetData::getPlanet)
                .filter(colony -> colony.galaxy.equals(finalSource.galaxy))
                .filter(colony -> colony.is(ColonyType.PLANET))
                .filter(colony -> !colony.equals(finalSource))
                .toList();

        return planetDataService.farms(source, others);
    }
}
