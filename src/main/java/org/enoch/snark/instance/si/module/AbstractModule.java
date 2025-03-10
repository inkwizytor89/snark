package org.enoch.snark.instance.si.module;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.enoch.snark.common.time.TimeScheduler;
import org.enoch.snark.instance.si.module.template.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.enoch.snark.instance.si.module.ThreadMap.*;

@Data
public class AbstractModule {

    private ModuleMap baseMap;
    private ModuleMap moduleMap;

    @Getter
    private Map<String, AbstractThread> threadsMap = new ConcurrentHashMap<>();
    @Getter
    private TimeScheduler timeScheduler = new TimeScheduler(OFF);
    @Setter
    protected ThreadMap mainMap;

    @Deprecated
    public static AbstractModule create(String moduleName, ModuleMap map) {
        if(moduleName.equals(GLOBAL)) return new Module(null);
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

    public AbstractModule(ModuleMap map) {
        updateMap(map);
        System.err.println("Constructor "+map.getName());
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

    public void updateMap(ThreadMap map) {
        timeScheduler.update(map.getConfig(TIME, OFF));
        this.mainMap = map;
    }

    public void destroy() {
        threadsMap.values().forEach(AbstractThread::destroy);
    }
}
