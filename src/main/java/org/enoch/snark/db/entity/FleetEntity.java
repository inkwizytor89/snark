package org.enoch.snark.db.entity;

import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.types.ColonyType;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "fleet", schema = "public", catalog = "snark")
public class FleetEntity extends IdEntity {

    public static final String SPY = "SPY";
    public static final String ATTACK = "ATTACK";
    public static final String EXPEDITION = "EXPEDITION";

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
    public String type;

    @ManyToOne
    @JoinColumn(name = "source_id", referencedColumnName = "id", nullable = false)
    public ColonyEntity source;

    @Basic
    @Column(name = "space_target")
    public String spaceTarget; // planet, moon, debris

    @Basic
    @Column(name = "start")
    public LocalDateTime start = LocalDateTime.now();

    @Basic
    @Column(name = "visited")
    public LocalDateTime visited;

    @Basic
    @Column(name = "back")
    public LocalDateTime back;

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
        start = LocalDateTime.now();
    }

    public boolean isDone() {
        LocalDateTime now = LocalDateTime.now();
        return (SPY.equals(type) && visited != null && now.isAfter(visited)) ||
                (ATTACK.equals(type) && back != null && now.isAfter(back));
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
        Integer spyLevel = target.spyLevel == null ? 4 : target.spyLevel;
        return createSpyFleet(target, spyLevel);
    }

    public static FleetEntity createSpyFleet(@Nonnull TargetEntity target,
                                             @Nonnull Integer count) {
        FleetEntity fleet = new FleetEntity();
        fleet.targetGalaxy = target.galaxy;
        fleet.targetSystem = target.system;
        fleet.targetPosition = target.position;
        fleet.source = Instance.getInstance().findNearestSource(target);
        fleet.type = SPY;
        fleet.espionageProbe = count.longValue();
        return fleet;
    }

    public static FleetEntity createFarmFleet(@Nonnull Instance instance,
                                             @Nonnull TargetEntity target) {
        FleetEntity fleet = new FleetEntity();
        fleet.targetGalaxy = target.galaxy;
        fleet.targetSystem = target.system;
        fleet.targetPosition = target.position;
        fleet.source = instance.findNearestSource(target);
        fleet.type = ATTACK;
        fleet.transporterSmall = target.calculateTransportByLt();
        return fleet;
    }

    public static FleetEntity createExpedition(@Nonnull ColonyEntity colony) {
        FleetEntity fleet = new FleetEntity();
        fleet.targetGalaxy = colony.galaxy;
        fleet.targetSystem = colony.system;
        fleet.targetPosition = 16;
        fleet.spaceTarget = ColonyType.PLANET.getName();
        fleet.source = colony;
        fleet.type = EXPEDITION;
        return fleet;
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
        return new Planet(this.targetGalaxy, this.targetSystem, this.targetPosition, ColonyType.parse(spaceTarget));
    }

    public void send() {
        if(targetGalaxy == null || targetSystem == null || targetPosition == null) {
            throw new RuntimeException("missing target for fleet in FleetBuilder");
        }
        if(source == null) {
            this.source = Instance.getInstance().findNearestSource(planet);
        }
        FleetDAO.getInstance().saveOrUpdate(this);
    }

    @Override
    public String toString() {
        return "[" + id + ": " + type + " " + source + " -> " + targetSystem.toString() + "]";
    }
}
