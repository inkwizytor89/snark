package org.enoch.snark.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "modules")
public class ModuleEntity extends IdEntity {

    @Column(name = "name", nullable = false, unique = true)
    public String name = "global";

    @Column(name = "time", nullable = false)
    public String time = "on";

    @Column(name = "conditions", nullable = false, columnDefinition = "TEXT")
    public String conditions = "";

    @Column(name = "created_on")
    public LocalDateTime createdOn = LocalDateTime.now();

    public ModuleEntity() {
    }

    public ModuleEntity(String name) {
        this.name = name;
    }
}
