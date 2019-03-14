package org.enoch.snark.db.entity;

import javax.persistence.*;

@MappedSuperclass
public abstract class BaseEntity extends IdEntity {

    @ManyToOne
    @JoinColumn(name = "universe_id", referencedColumnName = "id", nullable = false)
    public UniverseEntity universe;

}
