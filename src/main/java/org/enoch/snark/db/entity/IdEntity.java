package org.enoch.snark.db.entity;

import javax.persistence.Column;
import javax.persistence.Id;

public abstract class IdEntity {

    @Id
    @Column(name = "id")
    public Long id;

}
