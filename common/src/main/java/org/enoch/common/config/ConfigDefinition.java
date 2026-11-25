package org.enoch.common.config;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Describes a configuration option presented in {@link ThreadDefinition}.
 * It intentionally contains no module or thread; their context is provided by
 * ThreadDefinition and this value is used for display and schema/GUI purposes.
 * Use {@link ConfigEntry} to identify and set a configuration value.
 */
@Data
@NoArgsConstructor
public class ConfigDefinition {
    private String module;
    private String thread;
    private String key;
    private String value;
    private Class<?> type = String.class;
    private boolean temporary;
    private String description;

    public ConfigDefinition(String module, String thread, String key, String value, Class<?> type, boolean temporary, String description) {
        this.module = module;
        this.thread = thread;
        this.key = key;
        this.value = value;
        this.type = type;
        this.temporary = temporary;
        this.description = description;
    }

    public ConfigDefinition(
            String key,
            String value,
            boolean temporary,
            String description
    ) {
        this(null, null, key, value, String.class, temporary, description);
    }

    public ConfigDefinition(
            String key,
            String value,
            Class<?> type,
            boolean temporary,
            String description
    ) {
        this(null, null, key, value, type, temporary, description);
    }
}
