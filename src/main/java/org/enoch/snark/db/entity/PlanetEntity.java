package org.enoch.snark.db.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class PlanetEntity extends BaseEntity{
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
    @Column(name = "resources")
    public Long resources = 0L;

    @Basic
    @Column(name = "power")
    public Long power;

    public Integer lm;
    public Integer cm;
    public Integer kr;
    public Integer ow;
    public Integer pan;
    public Integer bom;
    public Integer ni;
    public Integer gs;
    public Integer mt;
    public Integer dt;
    public Integer kol;
    public Integer rec;
    public Integer son;
    public Integer sat;

    public Integer wr;
    public Integer ldl;
    public Integer cdl;
    public Integer dg;
    public Integer dj;
    public Integer wp;
    public Integer mpo;
    public Integer dpo;
    public Integer pr;
    public Integer mr;


    public PlanetEntity() {
        super();
    }

    public PlanetEntity(String input) {
        super();
        loadPlanetCoordinate(input);
    }

    public Integer calculateDistance(PlanetEntity planet) {
        if(!galaxy.equals(planet.galaxy)) {
            return roundDistance(galaxy, planet.galaxy, 6) *20000;
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
        galaxy = new Integer(numbersTable[GALAXY_INDEX]);
        system = new Integer(numbersTable[SYSTEM_INDEX]);
        position = new Integer(numbersTable[POSITION_INDEX]);
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

    @Override
    public String toString() {
        return "[" + galaxy + ", " + system + ", " + position + "]";
    }
}
