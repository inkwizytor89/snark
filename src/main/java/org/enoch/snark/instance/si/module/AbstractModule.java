package org.enoch.snark.instance.si.module;

import lombok.Getter;
import org.enoch.snark.instance.si.module.template.BuildModule;
import org.enoch.snark.instance.si.module.template.CleanPlanetsModule;
import org.enoch.snark.instance.si.module.template.SleepModule;
import org.enoch.snark.instance.si.module.template.TestModule;

import java.util.HashMap;
import java.util.Map;

import static org.enoch.snark.instance.si.module.ConfigMap.*;

@Getter
public abstract class AbstractModule {

    Map<String, AbstractThread> threadsMap = new HashMap<>();
    private final ModuleMap baseMap;

    public static AbstractModule create(String moduleName, ModuleMap map) {
        if(moduleName.equals(GLOBAL)) return new Module(map);
        else if (moduleName.equals(CleanPlanetsModule.NAME))
            return new CleanPlanetsModule(map);
        else if (moduleName.equals(SleepModule.NAME))
            return new CleanPlanetsModule(map);
        else if (moduleName.equals(BuildModule.NAME))
            return new CleanPlanetsModule(map);
        else if (moduleName.equals(TestModule.NAME))
            return new TestModule(map);
        else return null;
    }

    public AbstractModule(ModuleMap moduleMap) {
        baseMap = createBaseMap();
        updateMap(moduleMap);
    }

    public void updateMap(ModuleMap moduleMap) {
        ModuleMap actualMap = overrideBaseMap(moduleMap);
        actualMap.forEach((name, configMap) -> {
            if(MAIN.equals(name)) return;
            configMap.put("module_time", actualMap.get(MAIN).get(TIME));
            if(threadsMap.containsKey(name)) {
                threadsMap.get(name).updateMap(configMap);
            } else {
                AbstractThread thread = AbstractThread.create(configMap);
                threadsMap.put(name, thread);
                thread.start();
            }
        });
        threadsMap.entrySet().stream()
                .filter(entry -> !actualMap.containsKey(entry.getKey()))
                .forEach(thread -> thread.getValue().destroy());
    }

    protected abstract ModuleMap createBaseMap();

    protected ModuleMap overrideBaseMap(ModuleMap moduleMap) {
        return new ModuleMap(baseMap).override(moduleMap);
    }

    public void destroy() {
        threadsMap.values().forEach(AbstractThread::destroy);
    }
}
