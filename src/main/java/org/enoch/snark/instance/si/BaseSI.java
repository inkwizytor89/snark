package org.enoch.snark.instance.si;

import org.enoch.snark.common.RunningState;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.Commander;
import org.enoch.snark.instance.si.module.AbstractModule;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.PropertiesMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.si.module.ConfigMap.MAIN;

public class BaseSI extends Thread{
    private static BaseSI INSTANCE;

    private final Map<String, AbstractModule> modules= new HashMap<>();

    private BaseSI() {
        waitForEndOfInitialActions();
        start();
    }

    private void waitForEndOfInitialActions() {
        while(!Commander.getInstance().peekQueues().isEmpty()) SleepUtil.pause();
    }

    public static BaseSI getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new BaseSI();
        }
        return INSTANCE;
    }

    @Override
    public void run() {
        while(true) {
            PropertiesMap propertiesMap = Instance.getPropertiesMap();
            propertiesMap.forEach((moduleName, moduleMap) -> {
                if(modules.containsKey(moduleName)) {
                    modules.get(moduleName).updateMap(moduleMap);
                } else {
                    AbstractModule module = AbstractModule.create(moduleName, moduleMap);
                    modules.put(moduleName, module);
                }
            });
            modules.entrySet().stream()
                    .filter(entry -> !propertiesMap.containsKey(entry.getKey()))
                    .forEach(thread -> thread.getValue().destroy());
            SleepUtil.secondsToSleep(10L);
        }
    }

    public int getAvailableFleetCount(String withOutThreadName) {
        int fleetMax = Instance.commander.getFleetMax();
        if(fleetMax == 0) return 0;

        int fleetInUse = 0;
        List<AbstractThread> threads = modules.values().stream()
                .flatMap(abstractModule -> abstractModule.getThreadsMap().values().stream()).toList();
        for(AbstractThread thread : threads) {
            if(RunningState.isRunning(thread.getActualState()) && !thread.getName().equals(withOutThreadName)) {
                fleetInUse += thread.getRequestedFleetCount();
            }
        }
        return fleetMax - fleetInUse;
    }
}
