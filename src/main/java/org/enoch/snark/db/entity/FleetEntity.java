package org.enoch.snark.db.entity;

import org.enoch.snark.instance.Instance;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "fleet", schema = "public", catalog = "snark")
public class FleetEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "planet_id", referencedColumnName = "id", nullable = false)
    public TargetEntity target;

    @ManyToOne
    @JoinColumn(name = "source_id", referencedColumnName = "id", nullable = false)
    public SourceEntity source;

    @Basic
    @Column(name = "start")
    public Timestamp start = new Timestamp(System.currentTimeMillis());

    @Basic
    @Column(name = "visited")
    public Timestamp visited;

    @Basic
    @Column(name = "back")
    public Timestamp back;

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

    FleetEntity() {}

    public FleetEntity(Instance instance) {
        super();
        universe = instance.universeEntity;
    }

    public static FleetEntity createSpyFleet(@Nonnull Instance instance, @Nonnull TargetEntity target) {
        return createSpyFleet(instance, target, 1);
    }

    public static FleetEntity createSpyFleet(@Nonnull Instance instance,
                                             @Nonnull TargetEntity target,
                                             @Nonnull Integer count) {
        FleetEntity fleet = new FleetEntity(instance);
        fleet.target = target;
        fleet.source = instance.findNearestSource(target);
        fleet.son = count.longValue();
        return fleet;
    }
}
