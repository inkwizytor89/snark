package org.enoch.common.config;

import lombok.AllArgsConstructor;
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
