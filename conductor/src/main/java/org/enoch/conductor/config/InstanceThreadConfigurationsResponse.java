package org.enoch.conductor.config;

import org.enoch.common.config.ThreadDefinition;
import org.enoch.common.config.ModuleDefinition;

import java.util.List;

public record InstanceThreadConfigurationsResponse(
        String instanceId,
        List<ThreadDefinition> definitions,
        List<ModuleDefinition> modules
) {
    public InstanceThreadConfigurationsResponse(String instanceId, List<ThreadDefinition> definitions) {
        this(instanceId, definitions, List.of());
    }
}
