package org.enoch.snark.db.entity;

import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.gi.macro.BuildingEnum;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.Resources;
import org.enoch.snark.model.exception.TargetMissingResourceInfoException;
import org.enoch.snark.model.types.ColonyType;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.enoch.snark.module.ConfigMap.TRANSPORTER_SMALL_CAPACITY;

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
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    public ColonyType type = ColonyType.PLANET;

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
    @Column(name = "tags")
    public String tags;

    @Basic
    @Column(name = "is_planet")
    public boolean isPlanet = true;

    @Basic
    @Column(name = "power")
    public Long energy;

    @Basic
    @Column(name = "created")
    public LocalDateTime created = LocalDateTime.now();

    @Basic
    @Column(name = "debris_metal")
    public Long debrisMetal;

    @Basic
    @Column(name = "debris_crystal")
    public Long debrisCrystal;

    @Basic
    @Column(name = "debris_deuterium")
    public Long debrisDeuterium;

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

// Humans
    @Basic
    @Column(name = "lifeformTech11101")
    public Long lifeformTech11101;

    @Basic
    @Column(name = "lifeformTech11102")
    public Long lifeformTech11102;

    @Basic
    @Column(name = "lifeformTech11103")
    public Long lifeformTech11103;

    @Basic
    @Column(name = "lifeformTech11104")
    public Long lifeformTech11104;

    @Basic
    @Column(name = "lifeformTech11105")
    public Long lifeformTech11105;

    @Basic
    @Column(name = "lifeformTech11106")
    public Long lifeformTech11106;

    @Basic
    @Column(name = "lifeformTech11107")
    public Long lifeformTech11107;

    @Basic
    @Column(name = "lifeformTech11108")
    public Long lifeformTech11108;

    @Basic
    @Column(name = "lifeformTech11109")
    public Long lifeformTech11109;

    @Basic
    @Column(name = "lifeformTech11110")
    public Long lifeformTech11110;

    @Basic
    @Column(name = "lifeformTech11111")
    public Long lifeformTech11111;

    @Basic
    @Column(name = "lifeformTech11112")
    public Long lifeformTech11112;
    // Rock´tal
    @Basic
    @Column(name = "lifeformTech12101")
    public Long lifeformTech12101;

    @Basic
    @Column(name = "lifeformTech12102")
    public Long lifeformTech12102;

    @Basic
    @Column(name = "lifeformTech12103")
    public Long lifeformTech12103;

    @Basic
    @Column(name = "lifeformTech12104")
    public Long lifeformTech12104;

    @Basic
    @Column(name = "lifeformTech12105")
    public Long lifeformTech12105;

    @Basic
    @Column(name = "lifeformTech12106")
    public Long lifeformTech12106;

    @Basic
    @Column(name = "lifeformTech12107")
    public Long lifeformTech12107;

    @Basic
    @Column(name = "lifeformTech12108")
    public Long lifeformTech12108;

    @Basic
    @Column(name = "lifeformTech12109")
    public Long lifeformTech12109;

    @Basic
    @Column(name = "lifeformTech12110")
    public Long lifeformTech12110;

    @Basic
    @Column(name = "lifeformTech12111")
    public Long lifeformTech12111;

    @Basic
    @Column(name = "lifeformTech12112")
    public Long lifeformTech12112;

    // Mecha
    @Basic
    @Column(name = "lifeformTech13101")
    public Long lifeformTech13101;

    @Basic
    @Column(name = "lifeformTech13102")
    public Long lifeformTech13102;

    @Basic
    @Column(name = "lifeformTech13103")
    public Long lifeformTech13103;

    @Basic
    @Column(name = "lifeformTech13104")
    public Long lifeformTech13104;

    @Basic
    @Column(name = "lifeformTech13105")
    public Long lifeformTech13105;

    @Basic
    @Column(name = "lifeformTech13106")
    public Long lifeformTech13106;

    @Basic
    @Column(name = "lifeformTech13107")
    public Long lifeformTech13107;

    @Basic
    @Column(name = "lifeformTech13108")
    public Long lifeformTech13108;

    @Basic
    @Column(name = "lifeformTech13109")
    public Long lifeformTech13109;

    @Basic
    @Column(name = "lifeformTech13110")
    public Long lifeformTech13110;

    @Basic
    @Column(name = "lifeformTech13111")
    public Long lifeformTech13111;

    @Basic
    @Column(name = "lifeformTech13112")
    public Long lifeformTech13112;

// Kaelesh
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

            case lifeformTech11101: return lifeformTech11101;
            case lifeformTech11102: return lifeformTech11102;
            case lifeformTech11103: return lifeformTech11103;
            case lifeformTech11104: return lifeformTech11104;
            case lifeformTech11105: return lifeformTech11105;
            case lifeformTech11106: return lifeformTech11106;
            case lifeformTech11107: return lifeformTech11107;
            case lifeformTech11108: return lifeformTech11108;
            case lifeformTech11109: return lifeformTech11109;
            case lifeformTech11110: return lifeformTech11110;
            case lifeformTech11111: return lifeformTech11111;
            case lifeformTech11112: return lifeformTech11112;

            case lifeformTech12101: return lifeformTech12101;
            case lifeformTech12102: return lifeformTech12102;
            case lifeformTech12103: return lifeformTech12103;
            case lifeformTech12104: return lifeformTech12104;
            case lifeformTech12105: return lifeformTech12105;
            case lifeformTech12106: return lifeformTech12106;
            case lifeformTech12107: return lifeformTech12107;
            case lifeformTech12108: return lifeformTech12108;
            case lifeformTech12109: return lifeformTech12109;
            case lifeformTech12110: return lifeformTech12110;
            case lifeformTech12111: return lifeformTech12111;
            case lifeformTech12112: return lifeformTech12112;

            case lifeformTech13101: return lifeformTech13101;
            case lifeformTech13102: return lifeformTech13102;
            case lifeformTech13103: return lifeformTech13103;
            case lifeformTech13104: return lifeformTech13104;
            case lifeformTech13105: return lifeformTech13105;
            case lifeformTech13106: return lifeformTech13106;
            case lifeformTech13107: return lifeformTech13107;
            case lifeformTech13108: return lifeformTech13108;
            case lifeformTech13109: return lifeformTech13109;
            case lifeformTech13110: return lifeformTech13110;
            case lifeformTech13111: return lifeformTech13111;
            case lifeformTech13112: return lifeformTech13112;

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
        Long configCapacity = Instance.getMainConfigMap().getConfigLong(TRANSPORTER_SMALL_CAPACITY, -1L);
        long amount;
        if(configCapacity == -1L) {
            Long hyperspaceTechnology = PlayerDAO.getInstance().fetch(PlayerEntity.mainPlayer()).hyperspaceTechnology;
            amount = 5000 + (250 * (hyperspaceTechnology +1));
        } else {
            amount = configCapacity;
        }
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
        Long configCapacity = Instance.getMainConfigMap().getConfigLong(TRANSPORTER_SMALL_CAPACITY, -1L);
        long amount;
        if(configCapacity == -1L) {
            Long hyperspaceTechnology = PlayerDAO.getInstance().fetch(PlayerEntity.mainPlayer()).hyperspaceTechnology;
            amount = 25000 + (250 * (hyperspaceTechnology + 1));
        } else {
            amount = configCapacity * 5;
        }
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

    public Resources getResources() {
        return new Resources(metal, crystal, deuterium);
    }

    @Override
    public String toString() {
        String type = isPlanet?"p":"m";
        return type + "[" + galaxy + ", " + system + ", " + position + "]";
    }
}
