package org.enoch.snark.db.entity;

import javax.persistence.*;

@Entity
//todo to powinna być kolonia a powinna być encja na abstakcyjna planete
@Table(name = "planets", schema = "public", catalog = "snark")
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
    @Column(name = "power")
    public Long power;

    @OneToMany(mappedBy = "source")
    public SourceEntity source;

    @OneToMany(mappedBy = "target")
    public TargetEntity target;

    @Basic
    @Column(name = "lm")
    public Long lm;

    @Basic
    @Column(name = "cm")
    public Long cm;

    @Basic
    @Column(name = "kr;")
    public Long kr;

    @Basic
    @Column(name = "ow")
    public Long ow;

    @Basic
    @Column(name = "pan")
    public Long pan;

    @Basic
    @Column(name = "bom")
    public Long bom;

    @Basic
    @Column(name = "ni")
    public Long ni;

    @Basic
    @Column(name = "gs")
    public Long gs;

    @Basic
    @Column(name = "mt")
    public Long mt;

    @Basic
    @Column(name = "dt")
    public Long dt;

    @Basic
    @Column(name = "kol")
    public Long kol;

    @Basic
    @Column(name = "rec")
    public Long rec;

    @Basic
    @Column(name = "son")
    public Long son;

    @Basic
    @Column(name = "sat")
    public Long sat;


    @Basic
    @Column(name = "wr")
    public Long wr;

    @Basic
    @Column(name = "ldl")
    public Long ldl;

    @Basic
    @Column(name = "cdl")
    public Long cdl;

    @Basic
    @Column(name = "dg;")
    public Long dg;

    @Basic
    @Column(name = "dj")
    public Long dj;

    @Basic
    @Column(name = "wp")
    public Long wp;

    @Basic
    @Column(name = "mpo")
    public Long mpo;

    @Basic
    @Column(name = "dpo")
    public Long dpo;

    @Basic
    @Column(name = "pr;")
    public Long pr;

    @Basic
    @Column(name = "mr")
    public Long mr;


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
