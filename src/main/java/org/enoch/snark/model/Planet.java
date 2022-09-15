package org.enoch.snark.model;

import org.enoch.snark.instance.Instance;

public class Planet {
    public static final Integer GALAXY_INDEX = 1;
    public static final Integer SYSTEM_INDEX = 2;
    public static final Integer POSITION_INDEX = 3;
    public Integer galaxy;
    public Integer system;
    public Integer position;

    protected Planet() {
    }
    public Planet(String input) {
        loadPlanetCoordinate(input);
    }

    public Integer calculateDistance(Planet planet) {
        if(!galaxy.equals(planet.galaxy)) {
            int galaxyMax = Integer.parseInt(Instance.getInstance().universe.getConfig((Universe.GALAXY_MAX)));
            return roundDistance(galaxy, planet.galaxy, galaxyMax) *20000;
        }
        if(!system.equals(planet.system)) {
            return roundDistance(system, planet.system, 499) *95 +2700;
        }
        return roundDistance(position, planet.position, 15)*5+1000;
    }

    private int roundDistance(Integer x1, Integer x2, Integer max) {
        return Math.abs(x1 - x2) < max - Math.abs(x1 - x2) ?  Math.abs(x1 - x2) : max - Math.abs(x1 - x2);
    }

    protected void loadPlanetCoordinate(String coordinateString) {
        String[] numbersTable = coordinateString.split("\\D+");
        galaxy = Integer.parseInt(numbersTable[GALAXY_INDEX]);
        system = Integer.parseInt(numbersTable[SYSTEM_INDEX]);
        position = Integer.parseInt(numbersTable[POSITION_INDEX]);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Planet)) return false;

        final String string = this.toString();
        Planet anotherPlanet = (Planet) obj;
        return this.galaxy.equals(anotherPlanet.galaxy) && this.system.equals(anotherPlanet.system) && this.position.equals(anotherPlanet.position);
    }

    public static String getCordinate(Integer galaxy, Integer system, Integer position) {
        return "["+galaxy+":"+system+":"+position+"]";
    }

    @Override
    public String toString() {
        return getCordinate(galaxy, system, position);
    }
    public String toFileName() {
        return toString().replace(":","_");
    }

}
