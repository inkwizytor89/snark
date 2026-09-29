package org.enoch.conductor.config;

import java.util.List;

public record UpdateInstanceConfigsRequest(
        List<InstanceConfigDto> configs
) {
}
