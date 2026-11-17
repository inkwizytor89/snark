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
    private Class<?> type = String.class;
    private boolean temporary;
    private String description;

    public ConfigDefinition(
            String module,
            String thread,
            String key,
            String value,
            boolean temporary,
            String description
    ) {
        this(module, thread, key, value, String.class, temporary, description);
    }
}
