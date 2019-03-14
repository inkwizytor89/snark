package org.enoch.snark.db.entity;

import javax.persistence.*;

@MappedSuperclass
public abstract class IdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

}
