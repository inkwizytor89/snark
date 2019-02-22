package org.enoch.snark.db.entity;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "fleet", schema = "public", catalog = "snark")
public class FleetEntity {

    @Id
    @Column(name = "id")
    public Long id;

    @ManyToOne
    @JoinColumn(name = "universe_id", referencedColumnName = "id", nullable = false)
    public UniverseEntity universe;

    @ManyToOne
    @JoinColumn(name = "planet_id", referencedColumnName = "id", nullable = false)
    public PlanetEntity planet;

    @Basic
    @Column(name = "start")
    public Timestamp start;

    @Basic
    @Column(name = "visited")
    public Timestamp visited;

    @Basic
    @Column(name = "back")
    public Timestamp back;
}
