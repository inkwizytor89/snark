package org.enoch.common.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ThreadDefinition {
    private String name;
    private String module;
    private String type;
    private String description;
    private LocalDateTime createdOn;
    private LocalDateTime updated;
    private List<ConfigDefinition> configs = new ArrayList<>();

    public ThreadDefinition(String name, String type, String description, List<ConfigDefinition> configs) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.configs = configs == null ? new ArrayList<>() : configs;
    }
}
