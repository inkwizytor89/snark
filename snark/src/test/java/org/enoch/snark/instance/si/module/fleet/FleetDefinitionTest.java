package org.enoch.snark.instance.si.module.fleet;

import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.db.entity.ModuleEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.enoch.snark.instance.service.CoordinateExpressionService;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FleetDefinitionTest {

    @Test
    void appliesThreadConfigAfterModuleConfig() {
        FleetDefinition config = new FleetDefinition();
        ModuleEntity module = new ModuleEntity("fleet");
        ThreadEntity mainThread = new ThreadEntity("main", module, "main");
        ThreadEntity fleetThread = new ThreadEntity("worker", module, "fleet");

        config.update(List.of(
                new ConfigEntity(mainThread, "target", "module-target"),
                new ConfigEntity(mainThread, "mission", Mission.ATTACK.name())
        ));
        config.update(List.of(new ConfigEntity(fleetThread, "target", "thread-target")));

        assertEquals("thread-target", config.getTargetValue());
        assertEquals(Mission.ATTACK, config.getMissionValue());
    }

    @Test
    void defaultsToDefinitionValues() {
        FleetDefinition config = new FleetDefinition();

        assertEquals(CoordinateExpressionService.PLANETS, config.getEffectiveSource());
        assertEquals(Mission.STATIONED, config.getMissionValue());
        assertEquals(1, config.getShipsWavesValue().size());
    }
}
