package org.enoch.snark.db.entity;

import org.enoch.snark.instance.Instance;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "fleet_requests", schema = "public", catalog = "snark")
public class FleetRequestEntity extends BaseEntity {

    @Basic
    @Column(name = "code")
    public Long code;

    @OneToOne
    @JoinColumn(name = "fleet_id", referencedColumnName = "id", nullable = false)
    public FleetEntity fleet;

    public FleetRequestEntity(Instance instance) {
        universe = instance.universeEntity;
    }
}
