package org.enoch.snark.db.entity;

import jakarta.persistence.*;
import org.enoch.snark.config.ConfigTerm;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public void update(ConfigTerm term) {
        switch (term.getKey()) {
            case "time" -> time = term.getValue();
            case "pause" -> pause = term.getValue();
            case "debug" -> debug = term.getValue();
            case "command_limit" -> commandLimit = term.getValue();
            case "conditions" -> conditions = term.getValue();
            default -> {
                ConfigEntity config = configs.stream()
                        .filter(existing -> existing.key.equals(term.getKey()))
                        .findFirst()
                        .orElseGet(() -> {
                            ConfigEntity newConfig = new ConfigEntity();
                            addConfig(newConfig);
                            newConfig.key = term.getKey();
                            return newConfig;
                        });
                config.value = term.getValue();
            }
        }
    }
}
