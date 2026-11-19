package org.enoch.snark.instance.si.module.event;

import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.db.entity.ModuleEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventDefinitionTest {

    @Test
    void appliesThreadConfigAfterModuleConfig() {
        EventDefinition config = new EventDefinition();
        ModuleEntity module = new ModuleEntity("event");
        ThreadEntity mainThread = new ThreadEntity("main", module, "main");
        ThreadEntity eventThread = new ThreadEntity("worker", module, "event");

        config.update(List.of(new ConfigEntity(mainThread, EventDefinition.ELEMENTS, "4")));
        config.update(List.of(new ConfigEntity(eventThread, EventDefinition.ELEMENTS, "8")));

        assertEquals(8L, config.getElements());
    }

    @Test
    void defaultsToDefinitionValue() {
        assertEquals(0L, new EventDefinition().getElements());
    }
}
