package org.enoch.snark.db.entity;

import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.Planet;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fleet", schema = "public", catalog = "snark")
public class FleetEntity extends BaseEntity {

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
    public Long lm;

    @Column(name = "CM")
    public Long cm;

    @Column(name = "KR")
    public Long kr;

    @Column(name = "OW")
    public Long ow;

    @Column(name = "PAN")
    public Long pan;

    @Column(name = "BOM")
    public Long bom;

    @Column(name = "NI")
    public Long ni;

    @Column(name = "GS")
    public Long gs;

    @Column(name = "RE")
    public Long re;

    @Column(name = "PF")
    public Long pf;

    @Column(name = "LT")
    public Long lt;

    @Column(name = "DT")
    public Long dt;

    @Column(name = "KOL")
    public Long kol;

    @Column(name = "REC")
    public Long rec;

    @Column(name = "SON")
    public Long son;

    @Transient
    private Instance instance;

    @Transient
    private  Planet planet;


    public FleetEntity() {
        super();
        start = LocalDateTime.now();
    }

    public FleetEntity(Instance instance) {
        super();
        this.instance = instance;
        universe = instance.universeEntity;
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

    public static FleetEntity createSpyFleet(@Nonnull Instance instance, @Nonnull TargetEntity target) {
        return createSpyFleet(instance, target, target.spyLevel);
    }

    public static FleetEntity createSpyFleet(@Nonnull Instance instance,
                                             @Nonnull TargetEntity target,
                                             @Nonnull Integer count) {
        FleetEntity fleet = new FleetEntity(instance);
        fleet.targetGalaxy = target.galaxy;
        fleet.targetSystem = target.system;
        fleet.targetPosition = target.position;
        fleet.source = instance.findNearestSource(target);
        fleet.type = SPY;
        fleet.son = count.longValue();
        return fleet;
    }

    public static FleetEntity createFarmFleet(@Nonnull Instance instance,
                                             @Nonnull TargetEntity target) {
        FleetEntity fleet = new FleetEntity(instance);
        fleet.targetGalaxy = target.galaxy;
        fleet.targetSystem = target.system;
        fleet.targetPosition = target.position;
        fleet.source = instance.findNearestSource(target);
        fleet.type = ATTACK;
        fleet.lt = target.calculateTransportByLt();
        return fleet;
    }

    public static FleetEntity createExpeditionFleet(@Nonnull Instance instance,
                                              @Nonnull Planet target) {
        FleetEntity fleet = new FleetEntity(instance);
        fleet.targetGalaxy = target.galaxy;
        fleet.targetSystem = target.system;
        fleet.targetPosition = 16;
        fleet.source = instance.daoFactory.colonyDAO.fetchAll().stream()
                .filter(colonyEntity -> colonyEntity.galaxy.equals(target.galaxy) &&
                        colonyEntity.system.equals(target.system) &&
                        colonyEntity.position.equals(target.position))
                .findFirst().get();
    //instance.findNearestSource(target);
        fleet.type = EXPEDITION;
        fleet.pf = 1L;
        fleet.dt = instance.calcutateExpeditionSize();
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

    public void send() {
        if(targetGalaxy == null || targetSystem == null || targetPosition == null) {
            throw new RuntimeException("missing target for fleet in FleetBuilder");
        }
        if(source == null) {
            this.source = instance.findNearestSource(planet);
        }
        this.instance.daoFactory.fleetDAO.saveOrUpdate(this);
    }

    @Override
    public String toString() {
        return "[" + id + ": " + type + " " + source + " -> " + targetSystem.toString() + "]";
    }
}
