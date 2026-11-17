package org.enoch.snark.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "modules")
public class ModuleEntity extends IdEntity {

    @Column(name = "name", nullable = false, unique = true)
    public String name;

    @Column(name = "time")
    public String time;

    @Column(name = "conditions", columnDefinition = "TEXT")
    public String conditions;

    public ModuleEntity() {
    }

    public ModuleEntity(String name) {
        this.name = name;
    }
}
