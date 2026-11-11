package org.enoch.snark.db.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "threads")
public class ThreadEntity extends IdEntity {

    @Column(name = "name", nullable = false)
    public String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    public ModuleEntity module;

    @Column(name = "\"type\"", nullable = false)
    public String type;

    @Column(name = "created_on")
    public LocalDateTime createdOn = LocalDateTime.now();

    public ThreadEntity() {
    }

    public ThreadEntity(String name, ModuleEntity module, String type) {
        this.name = name;
        this.module = module;
        this.type = type;
    }
}
