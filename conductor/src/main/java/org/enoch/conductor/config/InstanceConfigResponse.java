package org.enoch.conductor.config;

import org.enoch.common.config.ConfigDefinition;

import java.util.List;

public record InstanceConfigResponse(
        String instanceId,
        List<ConfigDefinition> configs
) {
}

