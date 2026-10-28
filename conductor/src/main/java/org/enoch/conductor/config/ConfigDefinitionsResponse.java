package org.enoch.conductor.config;

import org.enoch.common.config.ConfigDefinition;

import java.util.List;

public record ConfigDefinitionsResponse(
        String instanceId,
        List<ConfigDefinition> definitions
) {
}
