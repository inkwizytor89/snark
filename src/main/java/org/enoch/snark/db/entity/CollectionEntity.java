package org.enoch.snark.db.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "collections", schema = "public", catalog = "snark")
public class CollectionEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "source_id", referencedColumnName = "id", nullable = false)
    public ColonyEntity source;

    @Basic
    @Column(name = "type")
    public String type; // moon or planet

    @Basic
    @Column(name = "start")
    public LocalDateTime start = LocalDateTime.now();

}
