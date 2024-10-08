package org.enoch.snark.db.entity;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.gi.types.ShipEnum;
import org.enoch.snark.instance.model.action.ColonyPlaner;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.model.types.ColonyType;

import javax.annotation.Nonnull;
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
        for(Map.Entry<ShipEnum, Long> entry : toApply.entrySet()) {
            if(entry.getKey() == ShipEnum.fighterLight) fighterLight = entry.getValue();
            else if(entry.getKey() == ShipEnum.fighterHeavy) fighterHeavy = entry.getValue();
            else if(entry.getKey() == ShipEnum.cruiser) cruiser = entry.getValue();
            else if(entry.getKey() == ShipEnum.battleship) battleship = entry.getValue();
            else if(entry.getKey() == ShipEnum.interceptor) interceptor = entry.getValue();
            else if(entry.getKey() == ShipEnum.bomber) bomber = entry.getValue();
            else if(entry.getKey() == ShipEnum.destroyer) destroyer = entry.getValue();
            else if(entry.getKey() == ShipEnum.deathstar) deathstar = entry.getValue();
            else if(entry.getKey() == ShipEnum.reaper) reaper = entry.getValue();
            else if(entry.getKey() == ShipEnum.explorer) explorer = entry.getValue();

            else if(entry.getKey() == ShipEnum.transporterSmall) transporterSmall = entry.getValue();
            else if(entry.getKey() == ShipEnum.transporterLarge) transporterLarge = entry.getValue();
            else if(entry.getKey() == ShipEnum.colonyShip) colonyShip = entry.getValue();
            else if(entry.getKey() == ShipEnum.recycler) recycler = entry.getValue();
            else if(entry.getKey() == ShipEnum.espionageProbe) espionageProbe = entry.getValue();
        }
    }

    public ShipsMap getShips() {
        ShipsMap shipsMap = new ShipsMap();
        for(ShipEnum shipEnum : ShipEnum.values()) {
            Long shipValue = getShipValue(shipEnum);
            if(shipValue != null && shipValue > 0) shipsMap.put(shipEnum, shipValue);
        }
        return shipsMap;
    }

    public Long getShipValue(ShipEnum shipEnum) {
        if(ShipEnum.fighterLight.equals(shipEnum)) return fighterLight;
        else if(ShipEnum.fighterHeavy.equals(shipEnum)) return fighterHeavy;
        else if(ShipEnum.cruiser.equals(shipEnum)) return cruiser;
        else if(ShipEnum.battleship.equals(shipEnum)) return battleship;
        else if(ShipEnum.interceptor.equals(shipEnum)) return interceptor;
        else if(ShipEnum.bomber.equals(shipEnum)) return bomber;
        else if(ShipEnum.destroyer.equals(shipEnum)) return destroyer;
        else if(ShipEnum.deathstar.equals(shipEnum)) return deathstar;
        else if(ShipEnum.reaper.equals(shipEnum)) return reaper;
        else if(ShipEnum.explorer.equals(shipEnum)) return explorer;
        else if(ShipEnum.transporterSmall.equals(shipEnum)) return transporterSmall;
        else if(ShipEnum.transporterLarge.equals(shipEnum)) return transporterLarge;
        else if(ShipEnum.colonyShip.equals(shipEnum)) return colonyShip;
        else if(ShipEnum.recycler.equals(shipEnum)) return recycler;
        else if(ShipEnum.espionageProbe.equals(shipEnum)) return espionageProbe;
        else if(ShipEnum.solarSatellite.equals(shipEnum)) return 0L;
        else throw new IllegalStateException("Unknown ShipEnum "+shipEnum);
    }


    public void closeFlyPlan() {
        start = LocalDateTime.now();
        back = LocalDateTime.now();
        code = 0L;
    }

    public static FleetEntity createSpyFleet(@Nonnull TargetEntity target) {
        Long spyLevel = target.player.spyLevel;
        Long espionageProbeCount = spyLevel == null ? 4 : spyLevel;
//        System.err.println("player "+ target.player.name + " spy level "+ spyLevel+" but count "+espionageProbeCount);
        return createSpyFleet(target, espionageProbeCount);
    }

    public static FleetEntity createSpyFleet(@Nonnull TargetEntity target,
                                             @Nonnull Long count) {
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

    public static FleetEntity createExpeditionDirection(@Nonnull ColonyEntity colony) {
        FleetEntity fleet = new FleetEntity();
        fleet.targetGalaxy = colony.galaxy;
        fleet.targetSystem = colony.system;
        fleet.targetPosition = 16;
        fleet.spaceTarget = ColonyType.PLANET;
        fleet.source = colony;
        fleet.mission = Mission.EXPEDITION;
        return fleet;
    }

    public static FleetEntity createQuickFleetSave(@Nonnull ColonyEntity colony, Planet target) {
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
