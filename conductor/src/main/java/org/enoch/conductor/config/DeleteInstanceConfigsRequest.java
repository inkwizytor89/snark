package org.enoch.conductor.config;

import java.util.List;

public record DeleteInstanceConfigsRequest(
        List<InstanceConfigDto> configs
) {
}
