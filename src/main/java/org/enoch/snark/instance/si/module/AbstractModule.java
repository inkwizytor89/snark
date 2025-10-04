package org.enoch.snark.instance.si.module;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.enoch.snark.common.Debug;
import org.enoch.snark.common.time.TimeScheduler;

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


    public AbstractModule(ModuleMap map) {
        updateMap(map);
    }

    public void updateMap(ModuleMap moduleMap) {
        Debug.log(moduleMap.get(MAIN), "Update ModuleMap "+moduleMap);
        timeScheduler.update(moduleMap.get(MAIN).getConfig(TIME, OFF));
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
