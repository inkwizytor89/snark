package org.enoch.snark.module;

import java.util.HashMap;
import java.util.Map;

import static org.enoch.snark.instance.config.Config.NAME;

public class ConfigMap extends HashMap<String, String> {

    public ConfigMap() {
        super();
    }

    public ConfigMap(Map map) {
        super(map);
    }

    public String name() {
        if(!this.containsKey(NAME)) {
            System.err.println("Missing name in ConfigMap");
            put(NAME, "Missing name in ConfigMap");
        }
        return this.get(NAME);
    }
}
