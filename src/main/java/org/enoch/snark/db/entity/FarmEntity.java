package org.enoch.snark.db.entity;

import javax.persistence.*;
import java.sql.Timestamp;

//@Entity
@Table(name = "farm_waves", schema = "public", catalog = "snark")
public class FarmEntity {

    @Id
    @Column(name = "id")
    public Long id;

    @ManyToOne
    @JoinColumn(name = "universe_id", referencedColumnName = "id", nullable = false)
    public UniverseEntity universe;

    @Basic
    @Column(name = "start")
    public Timestamp start;

    public SpyRequestEntity spyRequestEntity;

    public WarRequestEntity warRequestEntity;


}
