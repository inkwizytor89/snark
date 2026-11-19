package org.enoch.snark.instance.si.module.update;

import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.db.entity.ModuleEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateDefinitionTest {

    @Test
    void appliesThreadConfigAfterModuleConfigAndKeepsDefaultsForUnspecifiedValues() {
        UpdateDefinition config = new UpdateDefinition();
        ModuleEntity module = new ModuleEntity("update");
        ThreadEntity mainThread = new ThreadEntity("main", module, "main");
        ThreadEntity updateThread = new ThreadEntity("worker", module, "update");

        config.update(List.of(
                new ConfigEntity(mainThread, UpdateThread.REFRESH, "20M"),
                new ConfigEntity(mainThread, UpdateThread.CHECK_SPY_REPORTS_DURATION, "5M")
        ));
        config.update(List.of(
                new ConfigEntity(updateThread, UpdateThread.REFRESH, "8M")
        ));

        assertEquals("8M", config.getRefresh());
        assertEquals("5M", config.getCheckSpyReportsDuration());
    }
}
