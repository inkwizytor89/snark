package org.enoch.snark.instance.model.to;

import org.enoch.snark.instance.model.to.Planet;

public class SourcePlanet extends Planet {

    private static final Integer COORDINATE_INDEX = 0;
    private static final Integer PLANET_ID_INDEX = 1;
    public Integer planetId;

    public SourcePlanet(String informationString) {
        super();
        String[] dataTable = informationString.split("#");
        loadPlanetCoordinate(dataTable[COORDINATE_INDEX]);
        planetId = Integer.parseInt(dataTable[PLANET_ID_INDEX]);
    }
}
