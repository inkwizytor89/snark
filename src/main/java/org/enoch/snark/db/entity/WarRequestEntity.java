package org.enoch.snark.db.entity;

import javax.persistence.*;

@Entity
@Table(name = "planets", schema = "public", catalog = "snark")
public class WarRequestEntity {

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
    public FleetEntity fleetEntity;

}
