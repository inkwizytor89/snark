package org.enoch.snark.instance.si.module.define;

import lombok.Data;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.util.ArrayList;
import java.util.List;

import static org.enoch.snark.instance.si.module.ThreadMap.STRATEGY;

@Data
public class DefineDefinition {

    public static final String THREAD_TYPE = "define";
    public static final String NOT_NULL_STRATEGY = "not_null";

    private String strategy;

    public DefineDefinition() {
        for (ConfigDefinition config : threadDefinition().getConfigs()) {
            if (STRATEGY.equals(config.getKey())) {
                strategy = config.getValue();
            }
        }
    }

    public void update(List<ConfigEntity> configs) {
        for (ConfigEntity config : configs) {
            if (STRATEGY.equals(config.key)) {
                strategy = config.value;
            }
        }
    }

    public static ThreadDefinition threadDefinition() {
        List<ConfigDefinition> configs = new ArrayList<>();
        configs.add(new ConfigDefinition("define", THREAD_TYPE, STRATEGY, NOT_NULL_STRATEGY, false,
                "Strategy used when processing cached values and deciding whether empty results are accepted."));
        return new ThreadDefinition(THREAD_TYPE,
                "Writes and refreshes cache values derived from configured database queries.",
                configs);
    }
}
