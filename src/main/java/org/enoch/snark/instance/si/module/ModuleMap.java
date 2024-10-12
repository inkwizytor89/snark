package org.enoch.snark.instance.si.module;

import java.util.HashMap;

public class ModuleMap extends HashMap<String, ConfigMap> {
    public ModuleMap(){
        super();
    }

    public ModuleMap(ModuleMap newMap) {
        super(newMap);
    }

    public ModuleMap override(ModuleMap moduleMap) {
        moduleMap.forEach((name, configMap) -> {
            if(!this.containsKey(name))
                this.put(name, configMap);
            else
                this.get(name).putAll(configMap);
        });
        return this;
    }
}
