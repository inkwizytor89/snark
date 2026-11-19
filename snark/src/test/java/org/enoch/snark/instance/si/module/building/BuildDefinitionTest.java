package org.enoch.snark.instance.si.module.building;

import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.db.entity.ModuleEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.service.CoordinateExpressionService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildDefinitionTest {

    @Test
    void appliesThreadConfigAfterModuleConfig() {
        BuildDefinition config = new BuildDefinition();
        ModuleEntity module = new ModuleEntity("build");
        ThreadEntity mainThread = new ThreadEntity("main", module, "main");
        ThreadEntity buildThread = new ThreadEntity("worker", module, "build");

        config.update(List.of(
                new ConfigEntity(mainThread, BuildDefinition.LIST, "module-list"),
                new ConfigEntity(mainThread, BuildDefinition.SWAP_TRANSPORT, "false"),
                new ConfigEntity(mainThread, BuildDefinition.SOURCE, "#all_moons()"),
                new ConfigEntity(mainThread, BuildDefinition.LEAVE_PLANET_RESOURCES, "m100 c200 d300")
        ));
        config.update(List.of(
                new ConfigEntity(buildThread, BuildDefinition.LIST, "thread-list"),
                new ConfigEntity(buildThread, BuildDefinition.LEAVE_PLANET_RESOURCES, "m400 c500 d600")
        ));

        assertEquals("thread-list", config.getList());
        assertEquals(false, config.getSwapTransport());
        assertEquals("#all_moons()", config.getSource());
        assertEquals(400L, config.getLeaveResources(ColonyType.PLANET, Resources.nothing).metal);
        assertEquals(Resources.nothing, config.getLeaveResources(ColonyType.MOON, Resources.nothing));
    }

    @Test
    void defaultsToValuesDeclaredByBuildDefinition() {
        BuildDefinition config = new BuildDefinition();

        assertEquals(BuildDefinition.DEFAULT_LIST, config.getList());
        assertTrue(config.getSwapTransport());
        assertEquals(CoordinateExpressionService.PLANETS, config.getSource());
        assertEquals(Resources.nothing, config.getLeaveResources(ColonyType.PLANET, Resources.nothing));
        assertEquals(Resources.nothing, config.getLeaveResources(ColonyType.MOON, Resources.nothing));
    }
}
