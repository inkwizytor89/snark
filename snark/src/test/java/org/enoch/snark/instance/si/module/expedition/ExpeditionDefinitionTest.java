package org.enoch.snark.instance.si.module.expedition;

import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.db.entity.ModuleEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpeditionDefinitionTest {

    @Test
    void appliesThreadConfigAfterModuleConfig() {
        ExpeditionDefinition config = new ExpeditionDefinition();
        ModuleEntity module = new ModuleEntity("expedition");
        ThreadEntity mainThread = new ThreadEntity("main", module, "main");
        ThreadEntity expeditionThread = new ThreadEntity("worker", module, "expedition");

        config.update(List.of(new ConfigEntity(mainThread, ExpeditionDefinition.SYSTEM_SHIFT, "3")));
        config.update(List.of(new ConfigEntity(expeditionThread, ExpeditionDefinition.SYSTEM_SHIFT, "5")));

        assertEquals(5, config.getSystemShift());
        assertEquals(2500, config.getMaxDt());
        assertEquals(true, config.getBattleExtension());
    }

    @Test
    void appliesConfiguredShipsAndDefaultsToExpeditionThreadShips() {
        ExpeditionDefinition config = new ExpeditionDefinition();

        ModuleEntity module = new ModuleEntity("expedition");
        ThreadEntity expeditionThread = new ThreadEntity("worker", module, "expedition");
        config.update(List.of(new ConfigEntity(expeditionThread, ExpeditionDefinition.SHIPS, "explorer:2")));

        assertEquals(1, config.getShips().size());
        assertEquals(2L, config.getShips().get(org.enoch.snark.instance.model.technology.Ship.explorer));
    }
}
