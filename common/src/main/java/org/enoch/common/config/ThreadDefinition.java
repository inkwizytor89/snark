package org.enoch.common.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ThreadDefinition {
    private String thread;
    private String name;
    private String module;
    private String type;
    private String description;
    private String time;
    private String pause;
    private String debug;
    private String commandLimit;
    private String conditions;
    private LocalDateTime createdOn;
    private LocalDateTime updated;
    private List<ConfigDefinition> configs = new ArrayList<>();

    public ThreadDefinition(String thread, String description, List<ConfigDefinition> configs) {
        this.thread = thread;
        this.name = thread;
        this.description = description;
        this.configs = configs == null ? new ArrayList<>() : configs;
        this.type = thread;
        this.time = "off";
        this.pause = "10S";
        this.debug = "false";
        this.commandLimit = "-1";
        this.conditions = "";
    }
}
