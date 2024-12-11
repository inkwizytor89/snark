package org.enoch.snark.config;

import lombok.Data;

@Data
public class ConfigTerm {
    private String module;
    private String thread;
    private String key;
    private String value;

    public static ConfigTerm parse(String property, String value, String defaultModule) {
        ConfigTerm configTerm = new ConfigTerm();
        String[] elements = property.split("\\.");
        int elementsLength = elements.length;

        configTerm.module = elementsLength == 3 ? elements[elementsLength-3] : defaultModule;
        configTerm.thread = elements[elementsLength-2];
        configTerm.key = elements[elementsLength-1];
        configTerm.value = value;
        return configTerm;
    }

    @Override
    public String toString() {
        return module+"."+thread+"."+key+"="+value;
    }
}
