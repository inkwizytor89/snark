package org.enoch.common.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigDefinition {
    private String key;
    private String value;
    private Class<?> type = String.class;
    private boolean temporary;
    private String description;

    public ConfigDefinition(
            String key,
            String value,
            boolean temporary,
            String description
    ) {
        this(key, value, String.class, temporary, description);
    }
}
