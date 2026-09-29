package org.enoch.snark.db.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "targets_activity", schema = "public", catalog = "snark")
public class TargetActivityEntity extends IdEntity {

    @ManyToOne
    @JoinColumn(name = "target_id", referencedColumnName = "id", nullable = false)
    public TargetEntity target;

    @Basic
    @Column(name = "counter")
    public Long counter;

    @Basic
    @Column(name = "tags")
    public String tags;
}
