package org.enoch.snark.db.entity;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.model.technology.Ship;
import org.enoch.snark.instance.model.action.ColonyPlaner;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.model.types.ColonyType;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "fleet", schema = "public", catalog = "snark")
public class FleetEntity extends IdEntity {

    public static final Long DEFENCE_CODE = 1L;
    public static final Long FLEET_SAVE_CODE = 2L;
    public static final Long FLEET_THREAD = 3L;

    @Basic
    @Column(name = "target_galaxy")
    public Integer targetGalaxy;

    @Basic
    @Column(name = "target_system")
    public Integer targetSystem;

    @Basic
    @Column(name = "target_position")
    public Integer targetPosition;

    @Basic
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    public Mission mission;

    @ManyToOne
    @JoinColumn(name = "source_id", referencedColumnName = "id", nullable = false)
    public ColonyEntity source;

    @Basic
    @Column(name = "space_target")
    @Enumerated(EnumType.STRING)
    public ColonyType spaceTarget;

    @Basic
    @Column(name = "start")
    public LocalDateTime start;

    @Basic
    @Column(name = "visited")
    public LocalDateTime visited;

    @Basic
    @Column(name = "back")
    public LocalDateTime back;

    @Basic
    @Column(name = "speed")
    public Long speed;

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
    @Column(name = "acs_code")
    public String acsCode;

    //todo: code to remove - hash has enought information
    @Basic
    @Column(name = "code")
    public Long code;

    @Basic
    @Column(name = "hash_code")
    public String hash;

    @Column(name = "LM")
    public Long fighterLight;

    @Column(name = "CM")
    public Long fighterHeavy;

    @Column(name = "KR")
    public Long cruiser;

    @Column(name = "OW")
    public Long battleship;

    @Column(name = "PAN")
    public Long interceptor;

    @Column(name = "BOM")
    public Long bomber;

    @Column(name = "NI")
    public Long destroyer;

    @Column(name = "GS")
    public Long deathstar;

    @Column(name = "RE")
    public Long reaper;

    @Column(name = "PF")
    public Long explorer;

    @Column(name = "LT")
    public Long transporterSmall;

    @Column(name = "DT")
    public Long transporterLarge;

    @Column(name = "KOL")
    public Long colonyShip;

    @Column(name = "REC")
    public Long recycler;

    @Column(name = "SON")
    public Long espionageProbe;

    public FleetEntity() {
        super();
    }

    public FleetEntity(FleetPromise promise) {
        super();
        source = promise.getSource();
        setTarget(promise.getTarget());
        mission = promise.getMission();
        speed = promise.getSpeed();
    }

    public boolean isItBack() {
        return back != null && LocalDateTime.now().isAfter(back);
    }

    public void setShips(ShipsMap toApply) {
        for(Map.Entry<Ship, Long> entry : toApply.entrySet()) {
            if(entry.getKey() == Ship.fighterLight) fighterLight = entry.getValue();
            else if(entry.getKey() == Ship.fighterHeavy) fighterHeavy = entry.getValue();
            else if(entry.getKey() == Ship.cruiser) cruiser = entry.getValue();
            else if(entry.getKey() == Ship.battleship) battleship = entry.getValue();
            else if(entry.getKey() == Ship.interceptor) interceptor = entry.getValue();
            else if(entry.getKey() == Ship.bomber) bomber = entry.getValue();
            else if(entry.getKey() == Ship.destroyer) destroyer = entry.getValue();
            else if(entry.getKey() == Ship.deathstar) deathstar = entry.getValue();
            else if(entry.getKey() == Ship.reaper) reaper = entry.getValue();
            else if(entry.getKey() == Ship.explorer) explorer = entry.getValue();

            else if(entry.getKey() == Ship.transporterSmall) transporterSmall = entry.getValue();
            else if(entry.getKey() == Ship.transporterLarge) transporterLarge = entry.getValue();
            else if(entry.getKey() == Ship.colonyShip) colonyShip = entry.getValue();
            else if(entry.getKey() == Ship.recycler) recycler = entry.getValue();
            else if(entry.getKey() == Ship.espionageProbe) espionageProbe = entry.getValue();
        }
    }

    public ShipsMap getShips() {
        ShipsMap shipsMap = new ShipsMap();
        for(Ship ship : Ship.values()) {
            Long shipValue = getShipValue(ship);
            if(shipValue != null && shipValue > 0) shipsMap.put(ship, shipValue);
        }
        return shipsMap;
    }

    public Long getShipValue(Ship ship) {
        if(Ship.fighterLight.equals(ship)) return fighterLight;
        else if(Ship.fighterHeavy.equals(ship)) return fighterHeavy;
        else if(Ship.cruiser.equals(ship)) return cruiser;
        else if(Ship.battleship.equals(ship)) return battleship;
        else if(Ship.interceptor.equals(ship)) return interceptor;
        else if(Ship.bomber.equals(ship)) return bomber;
        else if(Ship.destroyer.equals(ship)) return destroyer;
        else if(Ship.deathstar.equals(ship)) return deathstar;
        else if(Ship.reaper.equals(ship)) return reaper;
        else if(Ship.explorer.equals(ship)) return explorer;
        else if(Ship.transporterSmall.equals(ship)) return transporterSmall;
        else if(Ship.transporterLarge.equals(ship)) return transporterLarge;
        else if(Ship.colonyShip.equals(ship)) return colonyShip;
        else if(Ship.recycler.equals(ship)) return recycler;
        else if(Ship.espionageProbe.equals(ship)) return espionageProbe;
        else if(Ship.solarSatellite.equals(ship)) return 0L;
        else throw new IllegalStateException("Unknown Ship "+ ship);
    }


    public void closeFlyPlan() {
        start = LocalDateTime.now();
        back = LocalDateTime.now();
        code = 0L;
    }

    public static FleetEntity createSpyFleet(TargetEntity target) {
        Long spyLevel = target.player.spyLevel;
        Long espionageProbeCount = spyLevel == null ? 4 : spyLevel;
//        System.err.println("player "+ target.player.name + " spy level "+ spyLevel+" but count "+espionageProbeCount);
        return createSpyFleet(target, espionageProbeCount);
    }

    public static FleetEntity createSpyFleet(TargetEntity target,
                                             Long count) {
        FleetEntity fleet = new FleetEntity();
        fleet.targetGalaxy = target.galaxy;
        fleet.targetSystem = target.system;
        fleet.targetPosition = target.position;
        fleet.spaceTarget = ColonyType.PLANET;
        if(!target.is(ColonyType.PLANET)) {
            fleet.spaceTarget = ColonyType.MOON;
        }
        fleet.source = new ColonyPlaner().getNearestColony(target);
        fleet.mission = Mission.SPY;
        fleet.espionageProbe = count;
        return fleet;
    }

    public static FleetEntity createFarmFleet(TargetEntity target) {
        FleetEntity fleet = new FleetEntity();
        fleet.targetGalaxy = target.galaxy;
        fleet.targetSystem = target.system;
        fleet.targetPosition = target.position;
        fleet.source = new ColonyPlaner().getNearestColony(target);
        fleet.mission = Mission.ATTACK;
        Long requiredTransporterSmall = target.calculateTransportByTransporterSmall();
//        if(fleet.source.transporterSmall < requiredTransporterSmall) {
//            requiredTransporterSmall = fleet.source.transporterSmall;
//        }
        fleet.transporterSmall = requiredTransporterSmall;
        if(fleet.transporterSmall == 0) {
            System.err.println("\nfor "+target+" try to send 0 lt ("+target.getResourceString()+")");
        }
        return fleet;
    }

    public static FleetEntity createExpeditionDirection(ColonyEntity colony) {
        FleetEntity fleet = new FleetEntity();
        fleet.targetGalaxy = colony.galaxy;
        fleet.targetSystem = colony.system;
        fleet.targetPosition = 16;
        fleet.spaceTarget = ColonyType.PLANET;
        fleet.source = colony;
        fleet.mission = Mission.EXPEDITION;
        return fleet;
    }

    public static FleetEntity createQuickFleetSave(ColonyEntity colony, Planet target) {
        FleetEntity fleetEntity = new FleetEntity();
        ColonyEntity source = ColonyDAO.getInstance().fetch(colony);
        fleetEntity.source = source;
        fleetEntity.setTarget(target);
        if(ColonyType.MOON.equals(target.type)) {
            fleetEntity.mission = Mission.STATIONED;
        } else {
            fleetEntity.mission = Mission.COLONIZATION;
        }
        fleetEntity.setShips(source.getShipsMap());

        fleetEntity.metal = Long.MAX_VALUE;
        fleetEntity.crystal = Long.MAX_VALUE;
        fleetEntity.deuterium = Long.MAX_VALUE;
        return fleetEntity;
    }

    public FleetEntity to(PlanetEntity planet) {
        return to(planet.toPlanet());
    }

    public FleetEntity to(Planet planet) {
        this.targetGalaxy = planet.galaxy;
        this.targetSystem = planet.system;
        this.targetPosition = planet.position;
        return this;
    }

    public Planet getDestination() {
        return new Planet(this.targetGalaxy, this.targetSystem, this.targetPosition, spaceTarget);
    }

    @Override
    public String toString() {
        String targetSystemString = targetSystem!=null? targetSystem.toString():null;
        return "[" + id + ": " + mission + " " + source + " -> " + targetSystemString + "]";
    }

    public void setTarget(Planet planet) {
        targetGalaxy = planet.galaxy;
        targetSystem = planet.system;
        targetPosition = planet.position;
        spaceTarget = planet.type;
    }

    public Planet getTarget() {
        return new Planet(targetGalaxy, targetSystem, targetPosition, spaceTarget);
    }
}
