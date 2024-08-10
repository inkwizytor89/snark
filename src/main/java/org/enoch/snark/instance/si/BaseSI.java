package org.enoch.snark.instance.si;

import org.enoch.snark.common.RunningState;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.Commander;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ConfigMap;

import java.util.*;

public class BaseSI extends Thread{
    private static BaseSI INSTANCE;

    private final Map<String, AbstractThread> threadsMap= new HashMap<>();

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
            HashMap<String, ConfigMap> globalMap = Instance.getPropertiesMap();
            globalMap.forEach((name, map) -> {
                if("main".equals(name)) return;
                if(threadsMap.containsKey(name)) {
                    threadsMap.get(name).updateMap(map);
                } else {
                    AbstractThread thread = AbstractThread.create(map);
                    threadsMap.put(name, thread);
                    thread.start();
                }
            });
            threadsMap.entrySet().stream()
                    .filter(entry -> !globalMap.containsKey(entry.getKey()))
                    .forEach(thread -> thread.getValue().destroy());
            SleepUtil.secondsToSleep(10L);
        }
    }

    public int getAvailableFleetCount(String withOutThreadName) {
        int fleetMax = Instance.commander.getFleetMax();
        if(fleetMax == 0) return 0;

        int fleetInUse = 0;
        for(AbstractThread thread : threadsMap.values()) {
            if(RunningState.isRunning(thread.getActualState()) && !thread.getName().equals(withOutThreadName)) {
                fleetInUse += thread.getRequestedFleetCount();
            }
        }
        return fleetMax - fleetInUse;
    }
}
