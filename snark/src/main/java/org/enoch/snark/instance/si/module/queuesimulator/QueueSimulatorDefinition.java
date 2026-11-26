package org.enoch.snark.instance.si.module.queuesimulator;

import lombok.Data;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.instance.si.module.ThreadConfig;

import java.util.ArrayList;
import java.util.List;

@Data
public class QueueSimulatorDefinition implements ThreadConfig {

    public void update(List<ConfigEntity> configs) {
    }

    public static ThreadDefinition threadDefinition() {
        return new ThreadDefinition(
                QueueSimulatorThread.threadType,
                QueueSimulatorThread.threadType,
                "Consumes commands locally without browser or network access and records every processed command.",
                new ArrayList<>());
    }

    @Override
    public ThreadDefinition threadConfiguration(String thread, String module) {
        ThreadDefinition configured = new ThreadDefinition(
                thread,
                QueueSimulatorThread.threadType,
                threadDefinition().getDescription(),
                new ArrayList<ConfigDefinition>());
        configured.setModule(module);
        return configured;
    }
}
