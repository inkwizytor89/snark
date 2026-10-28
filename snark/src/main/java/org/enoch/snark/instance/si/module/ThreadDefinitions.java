package org.enoch.snark.instance.si.module;

import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.action.processor.CommandProcessor;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.FleetRepository;
import org.enoch.snark.instance.WorkerLifecycle;
import org.enoch.snark.instance.service.ConditionChecker;
import org.enoch.snark.instance.service.FleetDispatcher;
import org.enoch.snark.instance.service.FleetService;
import org.enoch.snark.instance.service.ShipService;
import org.enoch.snark.instance.si.Core;
import org.enoch.snark.instance.si.module.building.BuildThread;
import org.enoch.snark.instance.si.module.consumer.ConsumerThread;
import org.enoch.snark.instance.si.module.defense.DefenseThread;
import org.enoch.snark.instance.si.module.define.DefineThread;
import org.enoch.snark.instance.si.module.event.EventThread;
import org.enoch.snark.instance.si.module.expedition.ExpeditionThread;
import org.enoch.snark.instance.si.module.fleet.FleetThread;
import org.enoch.snark.instance.si.module.hunting.HuntingThread;
import org.enoch.snark.instance.si.module.scan.ScanThread;
import org.enoch.snark.instance.si.module.space.SpaceThread;
import org.enoch.snark.instance.si.module.update.UpdateThread;

import java.util.List;
import java.util.Objects;

public final class ThreadDefinitions {

    private ThreadDefinitions() {
    }

    public static List<ThreadDefinition> availableDefinitions() {
        return List.of(
                new ConsumerThread(null, null, null).threadDefinition(),
                new UpdateThread(null).threadDefinition(),
                new DefineThread(null, null).threadDefinition(),
                new DefenseThread(null).threadDefinition(),
                new ExpeditionThread(null).threadDefinition(),
                new BuildThread(null, null).threadDefinition(),
                new SpaceThread().threadDefinition(),
                new ScanThread().threadDefinition(),
                new HuntingThread().threadDefinition(),
                new FleetThread(null, null, null, null, null).threadDefinition(),
                new EventThread().threadDefinition()
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
