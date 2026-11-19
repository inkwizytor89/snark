package org.enoch.snark.instance.si.module.scan;

import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.db.entity.ModuleEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScanDefinitionTest {

    @Test
    void appliesThreadConfigAfterModuleConfig() {
        ScanDefinition config = new ScanDefinition();
        ModuleEntity module = new ModuleEntity("scan");
        ThreadEntity mainThread = new ThreadEntity("main", module, "main");
        ThreadEntity scanThread = new ThreadEntity("worker", module, "scan");

        config.update(List.of(new ConfigEntity(mainThread, ScanDefinition.RANGE, "5")));
        config.update(List.of(new ConfigEntity(scanThread, ScanDefinition.RANGE, "10")));

        assertEquals("10", config.getRange());
        assertEquals("", config.getCoordinate());
    }
}
