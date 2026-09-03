package org.enoch.snark.instance.si.module;

import lombok.Getter;
import org.enoch.snark.config.ConfigTerm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class PropertiesMap extends HashMap<String, ModuleMap> {

    @Getter
    private List<ConfigTerm> configs =  new ArrayList<>();

    public Collection<ModuleMap> modules() {
        return this.values();
    }

    public PropertiesMap() {
        super();
    }

    public PropertiesMap(PropertiesMap propertiesMap) {
        super(propertiesMap);
    }

    public PropertiesMap(List<ConfigTerm> configs)  {
        super();
        addAll(configs);
    }

    public void addAll(List<ConfigTerm> configs) {
        this.configs.addAll(configs);
        for(ConfigTerm term : configs) {
            this.putIfAbsent(term.getModule(), new ModuleMap(term.getModule()));
            ModuleMap moduleMap = this.get(term.getModule());

            moduleMap.putIfAbsent(term.getThread(), new ThreadMap());
            ThreadMap threadMap = moduleMap.get(term.getThread());

            threadMap.putIfAbsent(ThreadMap.NAME, term.getThread());
            threadMap.putIfAbsent(ThreadMap.MODULE, term.getModule());
            threadMap.putIfAbsent(term.getKey(), term.getValue());
        }
    }
}
