package org.enoch.conductor.config;

import org.enoch.common.config.ConfigDefinition;

import java.util.List;

public record InstanceConfigDefinitionsResponse(
        String instanceId,
        List<ConfigDefinition> definitions
) {
}

