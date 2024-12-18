package org.enoch.snark.instance.si.module;

import lombok.Data;
import lombok.Getter;
import org.enoch.snark.instance.si.module.template.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.enoch.snark.instance.si.module.ThreadMap.*;

@Data
public class AbstractModule {

    @Getter
    private Map<String, AbstractThread> threadsMap = new ConcurrentHashMap<>();
    private ModuleMap baseMap;
    private ModuleMap moduleMap;

    @Deprecated
    public static AbstractModule create(String moduleName, ModuleMap map) {
        if(moduleName.equals(GLOBAL)) return new Module();
        else if (moduleName.equals(CleanPlanetsModule.NAME))
            return new CleanPlanetsModule(map);
        else if (moduleName.equals(SleepModule.NAME))
            return new SleepModule(map);
        else if (moduleName.equals(BuildModule.NAME))
            return new BuildModule(map);
        else if (moduleName.equals(DutyModule.NAME))
            return new BuildModule(map);
        else if (moduleName.equals(TestModule.NAME))
            return new TestModule(map);
        else return null;
    }

    public void updateMap(ModuleMap moduleMap) {
        // mysle ze niszczeni obiektów jest kompletnie nie tak i beany trzba też niszcyc
        threadsMap.entrySet().stream()
                .filter(entry -> !moduleMap.containsKey(entry.getKey()))
                .forEach(entry -> {
                    try {
                        // Wywołanie metody destroy
                        entry.getValue().destroy();
                    } finally {
                        // Usunięcie wątku z mapy
                        threadsMap.remove(entry.getKey());
                    }
                });
        this.moduleMap = moduleMap;
    }

    public void destroy() {
        threadsMap.values().forEach(AbstractThread::destroy);
    }
}
