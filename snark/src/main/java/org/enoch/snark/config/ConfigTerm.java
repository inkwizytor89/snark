package org.enoch.snark.config;

import lombok.Data;

@Data
public class ConfigTerm {
    private String module;
    private String thread;
    private String key;
    private String value;

    public static ConfigTerm parse(String configString) {
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
    
    public static ConfigTerm parse(String property, String value, String defaultModule) {
        ConfigTerm configTerm = new ConfigTerm();
        String[] elements = property == null ? new String[0] : property.split("\\.");
        int elementsLength = elements.length;

        if (elementsLength >= 3) {
            // 3+ elementy: module.thread.key
            configTerm.module = elements[elementsLength - 3];
            configTerm.thread = elements[elementsLength - 2];
            configTerm.key = elements[elementsLength - 1];
        } else if (elementsLength == 2) {
            // 2 elementy: thread.key, module = default
            configTerm.module = defaultModule;
            configTerm.thread = elements[0];
            configTerm.key = elements[1];
        } else if (elementsLength == 1) {
            // 1 element: key, thread = main, module = default
            configTerm.module = defaultModule;
            configTerm.thread = "main";
            configTerm.key = elements[0];
        } else {
            // 0 elementów
            configTerm.module = defaultModule;
            configTerm.thread = "main";
            configTerm.key = "";
        }
        configTerm.value = value;
        return configTerm;
    }

    @Override
    public String toString() {
        return module+"."+thread+"."+key+"="+value;
    }
}
