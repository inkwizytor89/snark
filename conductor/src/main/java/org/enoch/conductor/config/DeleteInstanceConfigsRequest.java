package org.enoch.conductor.config;

import org.enoch.common.config.ConfigEntry;

import java.util.List;

public record DeleteInstanceConfigsRequest(
        List<ConfigEntry> configs
) {
}

