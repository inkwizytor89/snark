package org.enoch.snark.db.entity;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "fleet_requests", schema = "public", catalog = "snark")
public class FleetRequestEntity extends BaseEntity {

    @Basic
    @Column(name = "code")
    public Long code;

    @OneToMany
    @JoinColumn(name = "fleet_id", referencedColumnName = "id", nullable = false)
    public Collection<FleetEntity> fleet;

}
