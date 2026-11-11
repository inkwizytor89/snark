package org.enoch.snark.db.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "config")
public class ConfigEntity extends IdEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thread_id", nullable = false)
    public ThreadEntity thread;

    @Column(name = "\"key\"", nullable = false)
    public String key;

    @Column(name = "\"value\"", nullable = false)
    public String value;

    public ConfigEntity() {
    }

    public ConfigEntity(ThreadEntity thread, String key, String value) {
        this.thread = thread;
        this.key = key;
        this.value = value;
    }
}