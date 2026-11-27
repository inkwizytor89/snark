package org.enoch.common.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ModuleDefinition {
    private String name;
    private String time;
    private String conditions;
    @JsonIgnore
    private LocalDateTime createdOn;
    @JsonIgnore
    private LocalDateTime updated;
    private List<ConfigDefinition> configs = new ArrayList<>();

    public ModuleDefinition(String name, String time, String conditions) {
        this.name = name;
        this.time = time;
        this.conditions = conditions;
    }
}
