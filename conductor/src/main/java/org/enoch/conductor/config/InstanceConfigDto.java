package org.enoch.conductor.config;

import org.enoch.common.config.ConfigDefinition;

@Deprecated
public class InstanceConfigDto extends ConfigDefinition {
    public InstanceConfigDto() {
        super();
    }

    public InstanceConfigDto(String module, String thread, String key, String value, boolean temporary, String description) {
        super(module, thread, key, value, temporary, description);
    }
}

