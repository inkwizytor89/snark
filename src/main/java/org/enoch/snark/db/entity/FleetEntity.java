package org.enoch.snark.db.entity;

import org.enoch.snark.instance.Instance;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fleet", schema = "public", catalog = "snark")
public class FleetEntity extends BaseEntity {

    public static final String SPY = "SPY";
    public static final String ATTACK = "ATTACK";

    @ManyToOne
    @JoinColumn(name = "target_id", referencedColumnName = "id", nullable = false)
    public TargetEntity target;

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

    FleetEntity() {
        start = LocalDateTime.now();
    }

    public FleetEntity(Instance instance) {
        super();
        universe = instance.universeEntity;
        start = LocalDateTime.now();
    }

    public boolean isDone() {
        LocalDateTime now = LocalDateTime.now();
        return (SPY.equals(type) && visited != null && now.isAfter(visited)) ||
                (ATTACK.equals(type) && back != null && now.isAfter(back));
    }

    public static FleetEntity createSpyFleet(@Nonnull Instance instance, @Nonnull TargetEntity target) {
        return createSpyFleet(instance, target, target.spyLevel);
    }

    public static FleetEntity createSpyFleet(@Nonnull Instance instance,
                                             @Nonnull TargetEntity target,
                                             @Nonnull Integer count) {
        FleetEntity fleet = new FleetEntity(instance);
        fleet.target = target;
        fleet.source = instance.findNearestSource(target);
        fleet.type = SPY;
        fleet.son = count.longValue();
        return fleet;
    }

    public static FleetEntity createFarmFleet(@Nonnull Instance instance,
                                             @Nonnull TargetEntity target) {
        FleetEntity fleet = new FleetEntity(instance);
        fleet.target = target;
        fleet.source = instance.findNearestSource(target);
        fleet.type = ATTACK;
        fleet.lt = target.calculateTransportByLt();
        return fleet;
    }

    @Override
    public String toString() {
        return "[" + id + ": " + type + " " + source + " -> " + target + "]";
    }
}
