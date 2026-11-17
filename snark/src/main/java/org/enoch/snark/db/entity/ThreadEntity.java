package org.enoch.snark.db.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "time")
    public String time;

    @Column(name = "pause")
    public String pause;

    @Column(name = "debug")
    public String debug;

    @Column(name = "command_limit")
    public String commandLimit;

    @Column(name = "conditions", columnDefinition = "TEXT")
    public String conditions;

    @Column(name = "created_on")
    public LocalDateTime createdOn = LocalDateTime.now();

    @OneToMany(mappedBy = "thread", fetch = FetchType.LAZY)
    public List<ConfigEntity> configs = new ArrayList<>();

    public ThreadEntity() {
    }

    public ThreadEntity(String name, ModuleEntity module, String type) {
        this.name = name;
        this.module = module;
        this.type = type;
    }

    public void addConfig(ConfigEntity config) {
        configs.add(config);
        config.thread = this;
    }

    public String getThreadIdentity() {
        return module.name + "." + name;
    }
}
