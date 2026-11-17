package org.enoch.snark.instance.si.module;

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
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
public class DatabaseThreadTypeRegistry {

    private final Map<String, Class<? extends AbstractThread>> types = Map.ofEntries(
            Map.entry("consumer", ConsumerThread.class),
            Map.entry("update", UpdateThread.class),
            Map.entry("define", DefineThread.class),
            Map.entry("defense", DefenseThread.class),
            Map.entry("expedition", ExpeditionThread.class),
            Map.entry("build", BuildThread.class),
            Map.entry("space", SpaceThread.class),
            Map.entry("scan", ScanThread.class),
            Map.entry("hunting", HuntingThread.class),
            Map.entry("fleet", FleetThread.class),
            Map.entry("event", EventThread.class),
            Map.entry("consumerthread", ConsumerThread.class),
            Map.entry("updatethread", UpdateThread.class),
            Map.entry("definethread", DefineThread.class),
            Map.entry("defensethread", DefenseThread.class),
            Map.entry("expeditionthread", ExpeditionThread.class),
            Map.entry("buildthread", BuildThread.class),
            Map.entry("spacethread", SpaceThread.class),
            Map.entry("scanthread", ScanThread.class),
            Map.entry("huntingthread", HuntingThread.class),
            Map.entry("fleetthread", FleetThread.class),
            Map.entry("eventthread", EventThread.class)
    );

    public Optional<Class<? extends AbstractThread>> resolve(String type) {
        if (type == null || type.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(types.get(type.trim().toLowerCase(Locale.ROOT)));
    }
}
