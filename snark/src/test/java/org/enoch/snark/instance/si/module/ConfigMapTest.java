package org.enoch.snark.instance.si.module;

import org.enoch.common.config.ConfigDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigMapTest {

    @Test
    void convertsValuesToTheDeclaredType() {
        ConfigMap configMap = new ConfigMap();
        configMap.put("enabled", new ConfigDefinition(
                "module", "thread", "enabled", "true", Boolean.class, false, ""));
        configMap.put("limit", new ConfigDefinition(
                "module", "thread", "limit", "42", Integer.class, false, ""));

        assertEquals(true, configMap.getValue("enabled"));
        assertEquals(42, configMap.getValue("limit"));
    }

    @Test
    void existingDefinitionsDefaultToStringValues() {
        ConfigMap configMap = new ConfigMap();
        configMap.put("name", new ConfigDefinition(
                "module", "thread", "name", "worker", false, ""));

        assertEquals(String.class, configMap.get("name").getType());
        assertEquals("worker", configMap.getValue("name"));
    }
}
