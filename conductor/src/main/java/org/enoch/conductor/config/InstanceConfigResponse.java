package org.enoch.conductor.config;

import java.util.List;

public record InstanceConfigResponse(
        String instanceId,
        List<InstanceConfigDto> configs
) {
}

