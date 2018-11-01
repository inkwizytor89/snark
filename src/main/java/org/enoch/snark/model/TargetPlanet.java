package org.enoch.snark.model;

public class TargetPlanet extends Planet {
    private static final Integer COORDINATE_INDEX = 0;
    public Boolean isSkipped = false;
    public static final Integer POWER_INDEX = 1;
    public Integer power = null;

    public TargetPlanet(String informationString) {
        super();
        if(informationString.startsWith("-")) {
            isSkipped = true;
            informationString = informationString.replace("-","");
        }
        String[] dataTable = informationString.split("\\s+");
        loadPlanetCoordinate(dataTable[COORDINATE_INDEX]);
        if(dataTable.length > 1) {
            power = new Integer(dataTable[POWER_INDEX].replaceAll("\\.", ""));
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
