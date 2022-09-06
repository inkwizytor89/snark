package org.enoch.snark.db.entity;

import org.enoch.snark.model.Planet;

import javax.persistence.*;
import java.util.Collection;

@MappedSuperclass
public abstract class PlanetEntity extends IdEntity{
    public static final Integer GALAXY_INDEX = 1;
    public static final Integer SYSTEM_INDEX = 2;
    public static final Integer POSITION_INDEX = 3;

    @Basic
    @Column(name = "galaxy")
    public Integer galaxy;

    @Basic
    @Column(name = "system")
    public Integer system;

    @Basic
    @Column(name = "position")
    public Integer position;

    @Basic
    @Column(name = "metal")
    public Long metal;

    @Basic
    @Column(name = "crystal")
    public Long crystal;

    @Basic
    @Column(name = "deuterium")
    public Long deuterium;

    @Basic
    @Column(name = "power")
    public Long power;

    @Basic
    @Column(name = "lm")
    public Long fighterLight = 0L;

    @Basic
    @Column(name = "cm")
    public Long fighterHeavy = 0L;

    @Basic
    @Column(name = "kr")
    public Long cruiser = 0L;

    @Basic
    @Column(name = "ow")
    public Long battleship = 0L;

    @Basic
    @Column(name = "pan")
    public Long interceptor = 0L;

    @Basic
    @Column(name = "bom")
    public Long bomber = 0L;

    @Basic
    @Column(name = "ni")
    public Long destroyer = 0L;

    @Basic
    @Column(name = "gs")
    public Long deathstar = 0L;

    @Basic
    @Column(name = "mt")
    public Long transporterSmall = 0L;

    @Basic
    @Column(name = "dt")
    public Long transporterLarge = 0L;

    @Basic
    @Column(name = "kol")
    public Long colonyShip = 0L;

    @Basic
    @Column(name = "rec")
    public Long recycler = 0L;

    @Basic
    @Column(name = "son")
    public Long espionageProbe = 0L;

    @Basic
    @Column(name = "sat")
    public Long sat = 0L;

    @Basic
    @Column(name = "pf")
    public Long explorer = 0L;

    @Basic
    @Column(name = "re")
    public Long reaper = 0L;

    @Basic
    @Column(name = "wr")
    public Long rocketLauncher = 0L;

    @Basic
    @Column(name = "ldl")
    public Long laserCannonLight = 0L;

    @Basic
    @Column(name = "cdl")
    public Long laserCannonHeavy = 0L;

    @Basic
    @Column(name = "dg")
    public Long gaussCannon = 0L;

    @Basic
    @Column(name = "dj")
    public Long ionCannon = 0L;

    @Basic
    @Column(name = "wp")
    public Long plasmaCannon = 0L;

    @Basic
    @Column(name = "mpo")
    public Long shieldDomeSmall = 0L;

    @Basic
    @Column(name = "dpo")
    public Long shieldDomeLarge = 0L;

    @Basic
    @Column(name = "pr")
    public Long missileInterceptor = 0L;

    @Basic
    @Column(name = "mr")
    public Long missileInterplanetary = 0L;

//    Resources
    @Basic
    @Column(name = "metalMine")
    public Long metalMine;
    @Basic
    @Column(name = "crystalMine")
    public Long crystalMine;

    @Basic
    @Column(name = "deuteriumSynthesizer")
    public Long deuteriumSynthesizer;

    @Basic
    @Column(name = "solarPlant")
    public Long solarPlant;

    @Basic
    @Column(name = "fusionPlant")
    public Long fusionPlant;

    @Basic
    @Column(name = "solarSatellite")
    public Long solarSatellite;

    @Basic
    @Column(name = "metalStorage")
    public Long metalStorage;

    @Basic
    @Column(name = "crystalStorage")
    public Long crystalStorage;

    @Basic
    @Column(name = "deuteriumStorage")
    public Long deuteriumStorage;

//    Facilities
    @Basic
    @Column(name = "roboticsFactory")
    public Long roboticsFactory;

    @Basic
    @Column(name = "shipyard")
    public Long shipyard;

    @Basic
    @Column(name = "researchLaboratory")
    public Long researchLaboratory;

    @Basic
    @Column(name = "allianceDepot")
    public Long allianceDepot;

    @Basic
    @Column(name = "missileSilo")
    public Long missileSilo;

    @Basic
    @Column(name = "naniteFactory")
    public Long naniteFactory;

    @Basic
    @Column(name = "terraformer")
    public Long terraformer;

    @Basic
    @Column(name = "repairDock")
    public Long repairDock;


//    Lifeform
    @Basic
    @Column(name = "lifeformTech14101")
    public Long lifeformTech14101;

    @Basic
    @Column(name = "lifeformTech14102")
    public Long lifeformTech14102;

    @Basic
    @Column(name = "lifeformTech14103")
    public Long lifeformTech14103;

    @Basic
    @Column(name = "lifeformTech14104")
    public Long lifeformTech14104;

    @Basic
    @Column(name = "lifeformTech14105")
    public Long lifeformTech14105;

    @Basic
    @Column(name = "lifeformTech14106")
    public Long lifeformTech14106;

    @Basic
    @Column(name = "lifeformTech14107")
    public Long lifeformTech14107;

    @Basic
    @Column(name = "lifeformTech14108")
    public Long lifeformTech14108;

    @Basic
    @Column(name = "lifeformTech14109")
    public Long lifeformTech14109;

    @Basic
    @Column(name = "lifeformTech14110")
    public Long lifeformTech14110;

    @Basic
    @Column(name = "lifeformTech14111")
    public Long lifeformTech14111;

    @Basic
    @Column(name = "lifeformTech14112")
    public Long lifeformTech14112;


    public PlanetEntity() {
        super();
    }

    public PlanetEntity(String input) {
        super();
        loadPlanetCoordinate(input);
    }

//    public Integer calculateDistance(PlanetEntity planet) {
//        return this.calculateDistance(planet.toPlanet());
//    }

//    public Integer calculateDistance(Planet planet) {
//        return planet.calculateDistance()
//        if(!galaxy.equals(planet.galaxy)) {
//            return roundDistance(galaxy, planet.galaxy, 6) *20000;
//        }
//        if(!system.equals(planet.system)) {
//            return roundDistance(system, planet.system, 499) *95 +2700;
//        }
//        return roundDistance(position, planet.position, 15)*5+1000;
//    }

    private int roundDistance(Integer x1, Integer x2, Integer max) {
        return Math.abs(x1 - x2) < max - Math.abs(x1 - x2) ?  Math.abs(x1 - x2) : max - Math.abs(x1 - x2);
    }

    @Deprecated
    protected void loadPlanetCoordinate(String coordinateString) {
        String[] numbersTable = coordinateString.split("\\D+");
        galaxy = Integer.parseInt(numbersTable[GALAXY_INDEX]);
        system = Integer.parseInt(numbersTable[SYSTEM_INDEX]);
        position = Integer.parseInt(numbersTable[POSITION_INDEX]);
    }

    public static Long parseResource(String input) {
        double base = 1;
        input = input.replace(".","");

        if(input.contains("Mln")) {
            base = 1000000;
            input = input.replace("Mln","");
            input = input.replace(",",".");
        }
        Double result = Double.parseDouble(input) * base;
        return result.longValue();
    }

    public String getCordinate() {
        return Planet.getCordinate(galaxy, system, position);
    }

    public Planet toPlanet() {
        return new Planet(this.getCordinate());
    }

    @Override
    public String toString() {
        return "[" + galaxy + ", " + system + ", " + position + "]";
    }
}
