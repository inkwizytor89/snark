package org.enoch.snark.instance.si.module.space;

import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.db.entity.ModuleEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.enoch.snark.instance.si.module.ThreadMap;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpaceDefinitionTest {

    @Test
    void appliesThreadConfigAfterModuleConfig() {
        SpaceDefinition config = new SpaceDefinition();
        ModuleEntity module = new ModuleEntity("space");
        ThreadEntity mainThread = new ThreadEntity("main", module, "main");
        ThreadEntity spaceThread = new ThreadEntity("worker", module, "space");

        config.update(List.of(new ConfigEntity(mainThread, ThreadMap.GALAXY_MAX, "3")));
        config.update(List.of(
                new ConfigEntity(spaceThread, ThreadMap.GALAXY_MAX, "5"),
                new ConfigEntity(spaceThread, ThreadMap.RANGE, "10")
        ));

        assertEquals(5, config.getGalaxyMax());
        assertEquals("10", config.getRange());
        assertTrue(config.isGalaxyMaxConfigured());
    }

    @Test
    void keepsGalaxyMaxUnsetUntilConfigured() {
        SpaceDefinition config = new SpaceDefinition();

        assertEquals(0, config.getGalaxyMax());
        assertFalse(config.isGalaxyMaxConfigured());
    }
}
