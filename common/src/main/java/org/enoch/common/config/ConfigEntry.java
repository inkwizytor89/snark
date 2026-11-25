package org.enoch.common.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Identifies a configuration value that should be updated.
 * Unlike {@link ConfigDefinition}, this class includes module and thread,
 * so it is suitable for set/delete operations sent to a snark instance.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigEntry {
    private String module;
    private String thread;
    private String key;
    private String value;

    public static ConfigEntry parse(String configString) {
        if (configString == null) return null;

        int idx = configString.indexOf('=');
        String property;
        String value = "";
        if (idx >= 0) {
            property = configString.substring(0, idx).trim();
            value = configString.substring(idx + 1);
        } else {
            property = configString.trim();
        }
        return parse(property, value, "global");
    }

    public static ConfigEntry parse(String property, String value, String defaultModule) {
        ConfigEntry entry = new ConfigEntry();
        String[] elements = property == null ? new String[0] : property.split("\\.");
        int elementsLength = elements.length;

        if (elementsLength >= 3) {
            entry.module = elements[elementsLength - 3];
            entry.thread = elements[elementsLength - 2];
            entry.key = elements[elementsLength - 1];
        } else if (elementsLength == 2) {
            entry.module = defaultModule;
            entry.thread = elements[0];
            entry.key = elements[1];
        } else if (elementsLength == 1) {
            entry.module = defaultModule;
            entry.thread = "main";
            entry.key = elements[0];
        } else {
            entry.module = defaultModule;
            entry.thread = "main";
            entry.key = "";
        }
        entry.value = value;
        return entry;
    }

    @Override
    public String toString() {
        return module + "." + thread + "." + key + "=" + value;
    }
}
