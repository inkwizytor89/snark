package org.enoch.snark.db.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;

@Entity
@Table(name = "planets", schema = "public", catalog = "snark")
public class SpyRequestEntity {

    @Id
    @Column(name = "id")
    public Long id;

    @ManyToOne
    @JoinColumn(name = "universe_id", referencedColumnName = "id", nullable = false)
    public UniverseEntity universe;

    @Basic
    @Column(name = "code")
    public Long code;

    @OneToMany
    @JoinColumn(name = "fleet_id", referencedColumnName = "id", nullable = false)
    public Collection<FleetEntity> fleet;

}
