package org.enoch.snark.config;

import org.enoch.common.config.ConfigEntry;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.instance.si.module.building.BuildDefinition;
import org.enoch.snark.instance.si.module.consumer.ConsumerDefinition;
import org.enoch.snark.instance.si.module.defense.DefenseDefinition;
import org.enoch.snark.instance.si.module.define.DefineDefinition;
import org.enoch.snark.instance.si.module.event.EventDefinition;
import org.enoch.snark.instance.si.module.expedition.ExpeditionDefinition;
import org.enoch.snark.instance.si.module.fleet.FleetDefinition;
import org.enoch.snark.instance.si.module.hunting.HuntingDefinition;
import org.enoch.snark.instance.si.module.queuesimulator.QueueSimulatorDefinition;
import org.enoch.snark.instance.si.module.scan.ScanDefinition;
import org.enoch.snark.instance.si.module.space.SpaceDefinition;
import org.enoch.snark.instance.si.module.update.UpdateDefinition;

import java.util.List;

public final class ThreadType {

    private static final List<ThreadDefinition> DEFINITIONS = List.of(
            ConsumerDefinition.threadDefinition(),
            UpdateDefinition.threadDefinition(),
            DefineDefinition.threadDefinition(),
            DefenseDefinition.threadDefinition(),
            ExpeditionDefinition.threadDefinition(),
            BuildDefinition.threadDefinition(),
            SpaceDefinition.threadDefinition(),
            ScanDefinition.threadDefinition(),
            HuntingDefinition.threadDefinition(),
            FleetDefinition.threadDefinition(),
            EventDefinition.threadDefinition(),
            QueueSimulatorDefinition.threadDefinition()
    );

    private ThreadType() {
    }

    public static List<ThreadDefinition> availableDefinitions() {
        return DEFINITIONS;
    }

    public static List<String> availableTypes() {
        return DEFINITIONS.stream()
                .map(ThreadDefinition::getType)
                .filter(type -> type != null && !type.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    public static String resolveType(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Thread type is required");
        }
        return availableTypes().stream()
                .filter(type -> type.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported thread type: " + value));
    }

    public static String resolve(ConfigEntry entry) {
        if (entry == null) {
            return resolveType("fleet");
        }
        if ("type".equalsIgnoreCase(entry.getKey())) {
            return resolveType(entry.getValue());
        }

        String threadName = entry.getThread();
        if (threadName == null || threadName.isBlank()) {
            return resolveType("fleet");
        }
        String normalizedThreadName = threadName.toLowerCase(java.util.Locale.ROOT);
        return availableTypes().stream()
                .filter(type -> normalizedThreadName.contains(type.toLowerCase(java.util.Locale.ROOT)))
                .findFirst()
                .orElseGet(() -> resolveType("fleet"));
    }
}
