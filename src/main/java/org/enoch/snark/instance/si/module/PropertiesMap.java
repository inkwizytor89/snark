package org.enoch.snark.instance.si.module;

import java.util.Collection;
import java.util.HashMap;

public class PropertiesMap extends HashMap<String, ModuleMap> {

    public Collection<ModuleMap> modules() {
        return this.values();
    }
}
