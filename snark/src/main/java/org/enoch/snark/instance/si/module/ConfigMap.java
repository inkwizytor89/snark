package org.enoch.snark.instance.si.module;

import org.enoch.common.config.ConfigDefinition;
import org.enoch.snark.db.entity.ConfigEntity;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigMap extends HashMap<String, ConfigDefinition> {

    public void updateDefinitions(Collection<ConfigDefinition> configDefinitions) {
        configDefinitions.forEach(configDefinition -> {
            String key = configDefinition.getKey();
            if (this.containsKey(key)) {
                this.get(key).setValue(configDefinition.getValue());
            } else {
                this.put(key, configDefinition);
            }
        });
    }
    public void update(Collection<ConfigEntity> configDefinitions) {
        configDefinitions.forEach(configDefinition -> {
            String key = configDefinition.key;
            if (this.containsKey(key)) {
                this.get(key).setValue(configDefinition.value);
            }
        });
    }

    public Object getValue(String key) {
        ConfigDefinition definition = get(key);
        if (definition == null) {
            return null;
        }
        return convertValue(definition, Objects.requireNonNull(
                definition.getType(), "Config type must not be null for key '" + key + "'"));
    }

    private <T> T convertValue(ConfigDefinition definition, Class<T> type) {
        return DefaultConversionService.getSharedInstance().convert(definition.getValue(), type);
    }

    @Override
    public String toString() {
        return "{"+this.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "))+"}";
    }
}
