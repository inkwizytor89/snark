package org.enoch.snark.db.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class IdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Basic
    @Column(name = "updated")
    public LocalDateTime updated = LocalDateTime.now();

}
