package org.enoch.snark.instance.si.module.hunting;

import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.instance.si.module.ThreadConfig;

import java.util.Collections;

public final class HuntingDefinition implements ThreadConfig {

    public static final String THREAD_TYPE = "hunting";

    public HuntingDefinition() {
    }

    public static ThreadDefinition threadDefinition() {
        return new ThreadDefinition("hunting", "hunting",
                "Monitors active players and prioritizes hunt targets based on the current high-score state.", Collections.emptyList());
    }

    @Override
    public ThreadDefinition threadConfiguration(String thread, String module) {
        ThreadDefinition configured = threadDefinition();
        configured.setName(thread);
        configured.setModule(module);
        return configured;
    }
}
