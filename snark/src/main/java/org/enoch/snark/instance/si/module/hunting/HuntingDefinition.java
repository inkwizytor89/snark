package org.enoch.snark.instance.si.module.hunting;

import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.util.Collections;

public final class HuntingDefinition {

    public static final String THREAD_TYPE = "hunting";

    private HuntingDefinition() {
    }

    public static ThreadDefinition threadDefinition() {
        return new ThreadDefinition("hunting",
                "Monitors active players and prioritizes hunt targets based on the current high-score state.", Collections.emptyList());
    }
}
