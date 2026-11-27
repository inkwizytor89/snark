package org.enoch.snark.db.entity;

import jakarta.persistence.*;
import org.enoch.common.config.ConfigEntry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "threads")
public class ThreadEntity extends IdEntity {

    @Column(name = "name", nullable = false)
    public String name = "main";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    public ModuleEntity module;

    @Column(name = "\"type\"", nullable = false)
    public String type = "fleet";

    @Column(name = "time", nullable = false)
    public String time = "off";

    @Column(name = "pause", nullable = false)
    public String pause = "10S";

    @Column(name = "debug", nullable = false)
    public String debug = "false";

    @Column(name = "command_limit", nullable = false)
    public String commandLimit = "-1";

    @Column(name = "conditions", nullable = false, columnDefinition = "TEXT")
    public String conditions = "";

    @Column(name = "created_on")
    public LocalDateTime createdOn = LocalDateTime.now();

    @OneToMany(mappedBy = "thread", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    public List<ConfigEntity> configs = new ArrayList<>();

    public ThreadEntity() {
    }

    public ThreadEntity(String name, ModuleEntity module, String type) {
        this.name = name;
        this.module = module;
        this.type = type;
    }

    public void addConfig(ConfigEntity config) {
        config.thread = this;
        configs.add(config);
    }

    public String getThreadIdentity() {
        return module.name + "." + name;
    }

    public boolean applyUnsavedConfigUpdate(ConfigEntry term) {
        if (term == null || term.getKey() == null || term.getKey().isBlank()
                || term.getValue() == null) {
            throw new IllegalArgumentException("Configuration key and value are required");
        }
        String key = term.getKey().toLowerCase(java.util.Locale.ROOT);
        if ("time".equals(key)) {
            time = term.getValue();
        } else if ("type".equals(key)) {
            type = term.getValue();
        } else if ("pause".equals(key)) {
            pause = term.getValue();
        } else if ("debug".equals(key)) {
            debug = term.getValue();
        } else if ("command_limit".equals(key)) {
            commandLimit = term.getValue();
        } else if ("conditions".equals(key)) {
            conditions = term.getValue();
        } else {
            Optional<ConfigEntity> existing = findConfig(term.getKey());
            if (existing.isPresent()) {
                existing.get().value = term.getValue();
                return false;
            }
            new ConfigEntity(this, term.getKey(), term.getValue());
            return true;
        }
        return false;
    }

    public Optional<ConfigEntity> findConfig(String key) {
        if (key == null || configs == null) {
            return Optional.empty();
        }
        return configs.stream()
                .filter(config -> config != null && key.equalsIgnoreCase(config.key))
                .findFirst();
    }
}
