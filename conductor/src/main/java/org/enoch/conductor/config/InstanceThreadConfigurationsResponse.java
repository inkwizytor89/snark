package org.enoch.conductor.config;

import org.enoch.common.config.ThreadDefinition;

import java.util.List;

public record InstanceThreadConfigurationsResponse(
        String instanceId,
        List<ThreadDefinition> definitions
) {
}
