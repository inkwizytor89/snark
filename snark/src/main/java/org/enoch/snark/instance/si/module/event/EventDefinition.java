package org.enoch.snark.instance.si.module.event;

import lombok.Data;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.instance.si.module.ThreadConfig;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventDefinition implements ThreadConfig {

    public static final String THREAD_TYPE = "event";
    public static final String ELEMENTS = "elements";

    private Long elements;

    public EventDefinition() {
        for (ConfigDefinition config : threadDefinition().getConfigs()) {
            update(config.getKey(), config.getValue());
        }
    }

    public void update(List<ConfigEntity> configs) {
        for (ConfigEntity config : configs) {
            update(config.key, config.value);
        }
    }

    private void update(String key, String value) {
        if (ELEMENTS.equals(key) && value != null && !value.isBlank()) {
            elements = Long.valueOf(value);
        }
    }

    public static ThreadDefinition threadDefinition() {
        List<ConfigDefinition> configs = new ArrayList<>();
        configs.add(new ConfigDefinition(ELEMENTS, "0", false,
                "Stored event count used to detect a change in event volume."));
        return new ThreadDefinition(THREAD_TYPE,
                "Tracks important in-game events and raises a notification when the volume changes.",
                configs);
    }

    @Override
    public ThreadDefinition threadConfiguration() {
        ThreadDefinition definition = threadDefinition();
        List<ConfigDefinition> configs = new ArrayList<>();
        for (ConfigDefinition config : definition.getConfigs()) {
            String value = ELEMENTS.equals(config.getKey())
                    ? elements == null ? null : elements.toString()
                    : config.getValue();
            configs.add(new ConfigDefinition(
                    config.getKey(),
                    value,
                    config.getType(),
                    config.isTemporary(),
                    config.getDescription()));
        }
        return new ThreadDefinition(definition.getThread(), definition.getDescription(), configs);
    }
}
