package org.enoch.snark.instance.si.module.define;

import lombok.Data;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.instance.si.module.ThreadConfig;

import java.util.ArrayList;
import java.util.List;

import static org.enoch.snark.instance.si.module.ThreadMap.STRATEGY;

@Data
public class DefineDefinition implements ThreadConfig {

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
        configs.add(new ConfigDefinition(STRATEGY, NOT_NULL_STRATEGY, false,
                "Strategy used when processing cached values and deciding whether empty results are accepted."));
        return new ThreadDefinition(THREAD_TYPE, THREAD_TYPE,
                "Writes and refreshes cache values derived from configured database queries.",
                configs);
    }

    @Override
    public ThreadDefinition threadConfiguration(String thread, String module) {
        ThreadDefinition definition = threadDefinition();
        List<ConfigDefinition> configs = new ArrayList<>();
        for (ConfigDefinition config : definition.getConfigs()) {
            String value = STRATEGY.equals(config.getKey()) ? strategy : config.getValue();
            configs.add(new ConfigDefinition(
                    config.getKey(),
                    value,
                    config.getType(),
                    config.isTemporary(),
                    config.getDescription()));
        }
        ThreadDefinition configured = new ThreadDefinition(thread, definition.getType(), definition.getDescription(), configs);
        configured.setModule(module);
        return configured;
    }
}
