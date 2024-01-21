package org.enoch.snark.model;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.config.Config;
import org.enoch.snark.model.types.ColonyType;

import java.util.ArrayList;
import java.util.List;

import static org.enoch.snark.instance.config.Config.GALAXY_MAX;
import static org.enoch.snark.instance.config.Config.MAIN;

public class Planet {
    public static final Integer GALAXY_INDEX = 1;
    public static final Integer SYSTEM_INDEX = 2;
    public static final Integer POSITION_INDEX = 3;

    public ColonyType type = ColonyType.PLANET;
    public Integer galaxy;
    public Integer system;
    public Integer position;

    protected Planet() {
    }

    public Planet(String input) {
        this(input, 'm' == input.charAt(0) ? ColonyType.MOON : ColonyType.PLANET);
    }

    public Planet(String input, ColonyType type) {
        this.type = type;
        loadPlanetCoordinate(input);
    }

    public Planet(Integer galaxy, Integer system, Integer position) {
        this(galaxy, system, position, ColonyType.PLANET);
    }

    public Planet(Integer galaxy, Integer system, Integer position, ColonyType type) {
        this.galaxy = galaxy;
        this.system = system;
        this.position = position;
        this.type = type;
    }

    public static List<Planet> fromString(String code) {
        List<Planet> colonies = new ArrayList<>();
        String[] planetStrings = code.split(";");
        for(String string : planetStrings) {
            colonies.add(new Planet(string));
        }
        return colonies;
    }

    public Long calculateDistance(Planet planet) {
        if(!galaxy.equals(planet.galaxy)) {
            int galaxyMax = Instance.getMainConfigMap().getConfigInteger(GALAXY_MAX, 6);
            return roundDistance(galaxy, planet.galaxy, galaxyMax) *20000;
        } else if(!system.equals(planet.system)) {
            return roundDistance(system, planet.system, 499) *95 +2700;
        } else if(!position.equals(planet.position)) {
            return roundDistance(position, planet.position, 15)*5+1000;
        } else if(!type.equals(planet.type)) {
            return 5L;
        } else return 0L;
    }

    private Long roundDistance(Integer x1, Integer x2, Integer max) {
        return (long) Math.min(Math.abs(x1 - x2), max - Math.abs(x1 - x2));
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

        Planet anotherPlanet = (Planet) obj;
        return this.galaxy.equals(anotherPlanet.galaxy) &&
                this.system.equals(anotherPlanet.system) &&
                this.position.equals(anotherPlanet.position) &&
                this.type.equals(anotherPlanet.type);
    }

    public static String getCordinate(Planet planet) {
        return getCordinate(planet.galaxy, planet.system, planet.position);
    }

    public static String getCordinate(Integer galaxy, Integer system, Integer position) {
        return "["+galaxy+":"+system+":"+position+"]";
    }


    public ColonyEntity toColonyEntity() {
        return ColonyDAO.getInstance().get(this);
    }

    @Override
    public String toString() {
        return type.code()+getCordinate(galaxy, system, position);
    }
    public String toFileName() {
        return toString().replace(":","_");
    }

    public ColonyEntity getSimilarColony() {
        return new ColonyPlaner(ColonyDAO.getInstance().fetchAll()).findSimilar(this);
    }
}
