package org.enoch.snark.db.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

//@Data
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
