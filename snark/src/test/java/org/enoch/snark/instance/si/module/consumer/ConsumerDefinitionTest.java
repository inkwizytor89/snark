package org.enoch.snark.instance.si.module.consumer;

import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.db.entity.ModuleEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsumerDefinitionTest {

    @Test
    void updatesMatchingFieldsFromThreadConfigsAndPreservesDefaultsForOtherKeys() {
        ConsumerDefinition config = new ConsumerDefinition();
        ModuleEntity module = new ModuleEntity("global");
        ThreadEntity mainThread = new ThreadEntity("main", module, "main");
        ThreadEntity thread = new ThreadEntity("consumer", module, "consumer");
        List<ConfigEntity> moduleConfigs = List.of(
                new ConfigEntity(mainThread, ConsumerDefinition.LOGIN, "module-player"),
                new ConfigEntity(mainThread, ConsumerDefinition.RESTART_DURATION, "600S")
        );
        List<ConfigEntity> threadConfigs = List.of(
                new ConfigEntity(thread, ConsumerDefinition.LOGIN, "player"),
                new ConfigEntity(thread, ConsumerDefinition.WEBDRIVER_PATH, "driver.exe"),
                new ConfigEntity(thread, "source", "#all_moons()")
        );

        config.update(moduleConfigs);
        config.update(threadConfigs);

        assertEquals("player", config.getLogin());
        assertEquals("driver.exe", config.getWebdriverPath());
        assertEquals("600S", config.getRestartDuration());
        assertEquals("#all_moons()", config.getSource());
    }

    @Test
    void defaultsSourceToNoCoordinates() {
        assertEquals(org.enoch.snark.instance.service.CoordinateExpressionService.NONE,
                new ConsumerDefinition().getSource());
    }
}
