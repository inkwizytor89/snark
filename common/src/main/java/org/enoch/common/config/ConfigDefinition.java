package org.enoch.common.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigDefinition {
    private String module;
    private String thread;
    private String key;
    private String value;
    private boolean temporary;
    private String description;
}
