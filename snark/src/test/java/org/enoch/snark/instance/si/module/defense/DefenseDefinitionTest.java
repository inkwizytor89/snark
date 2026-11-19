package org.enoch.snark.instance.si.module.defense;

import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.db.entity.ModuleEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DefenseDefinitionTest {

    @Test
    void appliesThreadConfigAfterModuleConfig() {
        DefenseDefinition config = new DefenseDefinition();
        ModuleEntity module = new ModuleEntity("defense");
        ThreadEntity mainThread = new ThreadEntity("main", module, "main");
        ThreadEntity defenseThread = new ThreadEntity("worker", module, "defense");

        config.update(List.of(
                new ConfigEntity(mainThread, DefenseDefinition.ALARM, "module.wav"),
                new ConfigEntity(mainThread, DefenseDefinition.LIMIT, "1000")
        ));
        config.update(List.of(
                new ConfigEntity(defenseThread, DefenseDefinition.ALARM, "thread.wav"),
                new ConfigEntity(defenseThread, DefenseDefinition.RECALL, "5M")
        ));

        assertEquals("thread.wav", config.getAlarm());
        assertEquals(1000L, config.getLimit());
        assertEquals(300L, config.getRecallValue().getSeconds());
    }

    @Test
    void defaultsToDefinitionValues() {
        DefenseDefinition config = new DefenseDefinition();

        assertEquals(3000L, config.getLimit());
        assertEquals(AlarmSoundPlayer.MISSING_WAV, config.getAlarm());
        assertNull(config.getExampleTimeValue());
        assertNull(config.getExampleCoordinateValue());
    }
}
