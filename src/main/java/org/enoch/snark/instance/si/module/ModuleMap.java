package org.enoch.snark.instance.si.module;

import lombok.Data;

import java.util.Collection;
import java.util.HashMap;

@Data
public class ModuleMap extends HashMap<String, ThreadMap> {

    private String name;

    public ModuleMap(String name){
        super();
        this.name = name;
    }

    public Collection<ThreadMap> threads() {
        return this.values();
    }

    public Class<? extends AbstractModule> getTypeClass() {
        return Module.class;
    }


//    public ModuleMap(ModuleMap newMap) {
//        super(newMap);
//    }

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
