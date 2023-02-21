package org.enoch.snark.db.entity;

import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.gi.macro.BuildingEnum;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.exception.TargetMissingResourceInfoException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.HashMap;
import java.util.Map;

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
    @Column(name = "is_planet")
    public boolean isPlanet = true;

    @Basic
    @Column(name = "power")
    public Long energy;

    @Basic
    @Column(name = "fighterLight")
    public Long fighterLight = 0L;

    @Basic
    @Column(name = "fighterHeavy")
    public Long fighterHeavy = 0L;

    @Basic
    @Column(name = "cruiser")
    public Long cruiser = 0L;

    @Basic
    @Column(name = "battleship")
    public Long battleship = 0L;

    @Basic
    @Column(name = "interceptor")
    public Long interceptor = 0L;

    @Basic
    @Column(name = "bomber")
    public Long bomber = 0L;

    @Basic
    @Column(name = "destroyer")
    public Long destroyer = 0L;

    @Basic
    @Column(name = "deathstar")
    public Long deathstar = 0L;

    @Basic
    @Column(name = "transporterSmall")
    public Long transporterSmall = 0L;

    @Basic
    @Column(name = "transporterLarge")
    public Long transporterLarge = 0L;

    @Basic
    @Column(name = "colonyShip")
    public Long colonyShip = 0L;

    @Basic
    @Column(name = "recycler")
    public Long recycler = 0L;

    @Basic
    @Column(name = "espionageProbe")
    public Long espionageProbe = 0L;

    @Basic
    @Column(name = "sat")
    public Long sat = 0L;

    @Basic
    @Column(name = "explorer")
    public Long explorer = 0L;

    @Basic
    @Column(name = "reaper")
    public Long reaper = 0L;

    @Basic
    @Column(name = "rocketLauncher")
    public Long rocketLauncher = 0L;

    @Basic
    @Column(name = "laserCannonLight")
    public Long laserCannonLight = 0L;

    @Basic
    @Column(name = "laserCannonHeavy")
    public Long laserCannonHeavy = 0L;

    @Basic
    @Column(name = "gaussCannon")
    public Long gaussCannon = 0L;

    @Basic
    @Column(name = "ionCannon")
    public Long ionCannon = 0L;

    @Basic
    @Column(name = "plasmaCannon")
    public Long plasmaCannon = 0L;

    @Basic
    @Column(name = "shieldDomeSmall")
    public Long shieldDomeSmall = 0L;

    @Basic
    @Column(name = "shieldDomeLarge")
    public Long shieldDomeLarge = 0L;

    @Basic
    @Column(name = "missileInterceptor")
    public Long missileInterceptor = 0L;

    @Basic
    @Column(name = "missileInterplanetary")
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

    @Basic
    @Column(name = "moonbase")
    public Long moonbase;

    @Basic
    @Column(name = "sensorPhalanx")
    public Long sensorPhalanx;

    @Basic
    @Column(name = "jumpGate")
    public Long jumpGate;


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
        String in = input.replace(".","");
        if(in.contains("m") || in.contains("M")) {
            in = in.replaceAll("[a-zA-Z]", "");
            base = 1000000;
            in = in.replace(",",".");
        }
        Double result = Double.parseDouble(in) * base;
        long resultA = result.longValue();
        return resultA;
    }

    public String getCordinate() {
        return Planet.getCordinate(galaxy, system, position);
    }

    public Planet toPlanet() {
        return new Planet(this.toString());
    }

    public Map<ShipEnum, Long> getShipsMap() {
        Map<ShipEnum, Long> shipsMap = new HashMap<>();
        shipsMap.put(ShipEnum.fighterLight, fighterLight);
        shipsMap.put(ShipEnum.fighterHeavy, fighterHeavy);
        shipsMap.put(ShipEnum.cruiser, cruiser);
        shipsMap.put(ShipEnum.battleship, battleship);
        shipsMap.put(ShipEnum.interceptor, interceptor);
        shipsMap.put(ShipEnum.bomber, bomber);
        shipsMap.put(ShipEnum.destroyer, destroyer);
        shipsMap.put(ShipEnum.deathstar, deathstar);
        shipsMap.put(ShipEnum.reaper, reaper);
        shipsMap.put(ShipEnum.explorer, explorer);

        shipsMap.put(ShipEnum.transporterSmall, transporterSmall);
        shipsMap.put(ShipEnum.transporterLarge, transporterLarge);
        shipsMap.put(ShipEnum.colonyShip, colonyShip);
        shipsMap.put(ShipEnum.recycler, recycler);
        shipsMap.put(ShipEnum.espionageProbe, espionageProbe);

        return shipsMap;
    }

    public Long getBuildingLevel(BuildingEnum building) {
        switch (building) {
            case metalMine: return metalMine;
            case crystalMine: return crystalMine;
            case deuteriumSynthesizer: return deuteriumSynthesizer;
            case solarPlant: return solarPlant;
            case fusionPlant: return fusionPlant;
            case solarSatellite: return solarSatellite;
            case metalStorage: return metalStorage;
            case crystalStorage: return crystalStorage;
            case deuteriumStorage: return deuteriumStorage;

            case roboticsFactory: return roboticsFactory;
            case shipyard: return shipyard;
            case researchLaboratory: return researchLaboratory;
            case allianceDepot: return allianceDepot;
            case missileSilo: return missileSilo;
            case naniteFactory: return naniteFactory;
            case terraformer: return terraformer;
            case repairDock: return repairDock;

            case lifeformTech14101: return lifeformTech14101;
            case lifeformTech14102: return lifeformTech14102;
            case lifeformTech14103: return lifeformTech14103;
            case lifeformTech14104: return lifeformTech14104;
            case lifeformTech14105: return lifeformTech14105;
            case lifeformTech14106: return lifeformTech14106;
            case lifeformTech14107: return lifeformTech14107;
            case lifeformTech14108: return lifeformTech14108;
            case lifeformTech14109: return lifeformTech14109;
            case lifeformTech14110: return lifeformTech14110;
            case lifeformTech14111: return lifeformTech14111;
            case lifeformTech14112: return lifeformTech14112;

            default: throw new RuntimeException("Unknown Building");
        }
    }

    public Long calculateTransportByTransporterSmall() {
        if (this.metal == null || this.crystal == null || this.deuterium == null) {
            throw new TargetMissingResourceInfoException();
        }
        Long hyperspaceTechnology = PlayerDAO.getInstance().fetch(PlayerEntity.mainPlayer()).hyperspaceTechnology;
        long amount = 5000 + (250 * (hyperspaceTechnology +1));
        long ceil = (long) Math.ceil((double) (this.metal + this.crystal + this.deuterium) / amount);
//        System.err.println("planet = " + this.toString());
//        System.err.println("amount = " + amount);
//        System.err.println("size = " + (this.metal + this.crystal + this.deuterium));
//        System.err.println("sum = " + resources);
//        System.err.println("ships = "+ceil);
        if(ceil < 1) {
            System.err.println("Error: amount calculation fail for TransporterSmall");
        }
        return ceil;
    }

    public Long calculateTransportByTransporterLarge() {
        if (this.metal == null || this.crystal == null || this.deuterium == null) {
            throw new TargetMissingResourceInfoException();
        }
        Long hyperspaceTechnology = PlayerDAO.getInstance().fetch(PlayerEntity.mainPlayer()).hyperspaceTechnology;
        long amount = 25000 + (250 * (hyperspaceTechnology +1));
        long ceil = (long) Math.ceil((double) (this.metal + this.crystal + this.deuterium) / amount);
//        System.err.println("planet = " + this.toString());
//        System.err.println("amount = " + amount);
//        System.err.println("size = " + (this.metal + this.crystal + this.deuterium));
//        System.err.println("sum = " + resources);
//        System.err.println("ships = "+ceil);
        if(ceil < 1) {
            System.err.println("Error: amount calculation fail for TransporterLarge");
        }
        return ceil;
    }

    public String getResourceString() {
        return "m"+metal+" c"+crystal+" d"+deuterium;
    }

    @Override
    public String toString() {
        String type = isPlanet?"p":"m";
        return type + "[" + galaxy + ", " + system + ", " + position + "]";
    }
}
