package org.enoch.snark.db.entity;

import org.enoch.snark.model.Planet;

import javax.persistence.*;
import java.util.Collection;

@MappedSuperclass
public abstract class PlanetEntity extends BaseEntity{
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
    public Long lm = 0L;

    @Basic
    @Column(name = "cm")
    public Long cm = 0L;

    @Basic
    @Column(name = "kr")
    public Long kr = 0L;

    @Basic
    @Column(name = "ow")
    public Long ow = 0L;

    @Basic
    @Column(name = "pan")
    public Long pan = 0L;

    @Basic
    @Column(name = "bom")
    public Long bom = 0L;

    @Basic
    @Column(name = "ni")
    public Long ni = 0L;

    @Basic
    @Column(name = "gs")
    public Long gs = 0L;

    @Basic
    @Column(name = "mt")
    public Long mt = 0L;

    @Basic
    @Column(name = "dt")
    public Long dt = 0L;

    @Basic
    @Column(name = "kol")
    public Long kol = 0L;

    @Basic
    @Column(name = "rec")
    public Long rec = 0L;

    @Basic
    @Column(name = "son")
    public Long son = 0L;

    @Basic
    @Column(name = "sat")
    public Long sat = 0L;

    @Basic
    @Column(name = "pf")
    public Long pf = 0L;

    @Basic
    @Column(name = "re")
    public Long re = 0L;

    @Basic
    @Column(name = "wr")
    public Long wr = 0L;

    @Basic
    @Column(name = "ldl")
    public Long ldl = 0L;

    @Basic
    @Column(name = "cdl")
    public Long cdl = 0L;

    @Basic
    @Column(name = "dg")
    public Long dg = 0L;

    @Basic
    @Column(name = "dj")
    public Long dj = 0L;

    @Basic
    @Column(name = "wp")
    public Long wp = 0L;

    @Basic
    @Column(name = "mpo")
    public Long mpo = 0L;

    @Basic
    @Column(name = "dpo")
    public Long dpo = 0L;

    @Basic
    @Column(name = "pr")
    public Long pr = 0L;

    @Basic
    @Column(name = "mr")
    public Long mr = 0L;


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
