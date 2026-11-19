package org.enoch.snark.instance.si.module;

import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.instance.si.module.building.BuildDefinition;
import org.enoch.snark.instance.si.module.consumer.ConsumerDefinition;
import org.enoch.snark.instance.si.module.consumer.ConsumerThread;
import org.enoch.snark.instance.si.module.defense.DefenseDefinition;
import org.enoch.snark.instance.si.module.define.DefineDefinition;
import org.enoch.snark.instance.si.module.event.EventDefinition;
import org.enoch.snark.instance.si.module.expedition.ExpeditionDefinition;
import org.enoch.snark.instance.si.module.fleet.FleetDefinition;
import org.enoch.snark.instance.si.module.hunting.HuntingDefinition;
import org.enoch.snark.instance.si.module.scan.ScanDefinition;
import org.enoch.snark.instance.si.module.space.SpaceDefinition;
import org.enoch.snark.instance.si.module.update.UpdateDefinition;
import org.enoch.snark.instance.si.module.update.UpdateThread;

import java.util.List;
import java.util.Objects;

public final class ThreadDefinitions {

    private ThreadDefinitions() {
    }

    public static List<ThreadDefinition> availableDefinitions() {
        return List.of(
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
                EventDefinition.threadDefinition()
        );
    }

    public static List<String> availableThreadNames() {
        return availableDefinitions().stream()
                .map(ThreadDefinition::getThread)
                .filter(Objects::nonNull)
                .sorted()
                .toList();
    }

    public static ThreadDefinition byThread(String threadName) {
        if (threadName == null || threadName.isBlank()) {
            return null;
        }
        return availableDefinitions().stream()
                .filter(definition -> threadName.equals(definition.getThread()))
                .findFirst()
                .orElse(null);
    }
}
