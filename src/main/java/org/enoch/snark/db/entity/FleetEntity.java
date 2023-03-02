package org.enoch.snark.db.entity;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.ColonyPlaner;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.types.ColonyType;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "fleet", schema = "public", catalog = "snark")
public class FleetEntity extends IdEntity {

    public static final Long DEFENCE_CODE = 1L;
    public static final Long FLEET_SAVE_CODE = 2L;

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

    @Basic
    @Column(name = "code")
    public Long code;

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

    @Transient
    private  Planet planet;

    public FleetEntity() {
        super();
    }

    public boolean isItBack() {
        return back != null && LocalDateTime.now().isAfter(back);
    }

    public String getCoordinate() {
        return "[" + targetGalaxy + ", " + targetSystem + ", " + targetPosition + "]";
    }

    public void setShips(Map<ShipEnum, Long> toApply) {
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
        if(!target.isPlanet) {
            fleet.spaceTarget = ColonyType.MOON;
        }
        fleet.source = new ColonyPlaner(target).getNearestColony();
        fleet.mission = Mission.SPY;
        fleet.espionageProbe = count;
        return fleet;
    }

    public static FleetEntity createFarmFleet(TargetEntity target) {
        FleetEntity fleet = new FleetEntity();
        fleet.targetGalaxy = target.galaxy;
        fleet.targetSystem = target.system;
        fleet.targetPosition = target.position;
        fleet.source = new ColonyPlaner(target).getNearestColony();
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

    public static FleetEntity createExpedition(@Nonnull ColonyEntity colony) {
        FleetEntity fleet = new FleetEntity();
        fleet.targetGalaxy = colony.galaxy;
        fleet.targetSystem = colony.system;
        fleet.targetPosition = 16;
        fleet.spaceTarget = ColonyType.PLANET;
        fleet.source = colony;
        fleet.mission = Mission.EXPEDITION;
        return fleet;
    }

    public static FleetEntity createQuickColonization(@Nonnull ColonyEntity colony, Planet target) {
        FleetEntity fleetEntity = new FleetEntity();
        ColonyEntity source = ColonyDAO.getInstance().fetch(colony);
        fleetEntity.source = source;
        fleetEntity.setTarget(target);
        fleetEntity.mission = Mission.COLONIZATION;
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
        this.planet = planet;
        this.targetGalaxy = planet.galaxy;
        this.targetSystem = planet.system;
        this.targetPosition = planet.position;
        return this;
    }

    public Planet getDestination() {
        return new Planet(this.targetGalaxy, this.targetSystem, this.targetPosition, spaceTarget);
    }

    public void send() {
        if(targetGalaxy == null || targetSystem == null || targetPosition == null) {
            throw new RuntimeException("missing target for fleet in FleetBuilder");
        }
        if(source == null) {
            this.source = new ColonyPlaner(planet).getNearestColony();
        }
        FleetDAO.getInstance().saveOrUpdate(this);
    }

    @Override
    public String toString() {
        return "[" + id + ": " + mission + " " + source + " -> " + targetSystem.toString() + "]";
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
