package org.enoch.conductor.config;

import java.util.List;

public record InstanceConfigDefinitionsResponse(
        String instanceId,
        List<InstanceConfigDto> definitions
) {
}
