package org.enoch.snark.instance.si.module.define;

import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.db.entity.ModuleEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefineDefinitionTest {

    @Test
    void appliesThreadStrategyAfterModuleStrategy() {
        DefineDefinition config = new DefineDefinition();
        ModuleEntity module = new ModuleEntity("define");
        ThreadEntity mainThread = new ThreadEntity("main", module, "main");
        ThreadEntity defineThread = new ThreadEntity("cache", module, "define");

        config.update(List.of(new ConfigEntity(mainThread, "strategy", "any")));
        config.update(List.of(new ConfigEntity(defineThread, "strategy", "not_null")));

        assertEquals("not_null", config.getStrategy());
    }

    @Test
    void defaultsStrategyFromItsThreadDefinition() {
        assertEquals("not_null", new DefineDefinition().getStrategy());
    }
}
