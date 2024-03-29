package org.enoch.snark.instance.si;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.Commander;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ConfigMap;

import java.util.*;
import java.util.stream.Collectors;

public class BaseSI extends Thread{
    private static BaseSI INSTANCE;

    private final Map<String, AbstractThread> threadsMap= new HashMap<>();

    private BaseSI() {
//        HashMap<String, ConfigMap> globalMap = Instance.config.globalMap;
//        operationThreads.add(new UpdateThread(globalMap.get(UpdateThread.threadName)));
//        operationThreads.add(new DefenseThread(globalMap.get(DefenseThread.threadName)));
//        operationThreads.add(new FleetSaveThread(globalMap.get(FleetSaveThread.threadName)));
//        operationThreads.add(new ExpeditionThread(globalMap.get(ExpeditionThread.threadName)));
//        operationThreads.add(new BuildingThread(globalMap.get(BuildingThread.threadName)));
//        operationThreads.add(new FormsThread(globalMap.get(FormsThread.threadName)));
//        operationThreads.add(new SpaceThread(globalMap.get(SpaceThread.threadName))); // explore space
//        operationThreads.add(new ScanThread(globalMap.get(ScanThread.threadName))); // checking i-player on defence
//        operationThreads.add(new FarmThread(globalMap.get(FarmThread.threadName))); // in progress
//        operationThreads.add(new CollectorThread(globalMap.get(CollectorThread.threadName))); // in progress
//        operationThreads.add(new HuntingThread(globalMap.get(HuntingThread.threadName)));

//        while(Navigator.getInstance().getEventFleetList() == null) SleepUtil.pause();
        waitForEndOfInitialActions();
//        operationThreads.forEach(Thread::start);
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
            HashMap<String, ConfigMap> globalMap = Instance.config.globalMap;
            globalMap.forEach((s, map) -> {
                if("main".equals(s)) return;
                if(threadsMap.containsKey(s)) {
                    threadsMap.get(s).updateMap(map);
                } else {
                    AbstractThread thread = AbstractThread.create(map);
                    threadsMap.put(s, thread);
                    thread.start();
                }// co z tym jak konfiguracja zniknie ?
            });
            List<String> threadToRemove = threadsMap.keySet().stream()
                    .filter(s -> !globalMap.containsKey(s))
                    .collect(Collectors.toList());
// todo: zatrzymać je

//            operationThreads.forEach(thread -> thread.updateMap(new ConfigMap()));
            SleepUtil.secondsToSleep(10L);
        }
    }

    public int getAvailableFleetCount(String withOutThreadName) {
        int fleetMax = Instance.commander.getFleetMax();
        if(fleetMax == 0) return 0;

        int fleetInUse = 0;
        for(AbstractThread thread : threadsMap.values()) {
            if(thread.isRunning() && !thread.getThreadName().equals(withOutThreadName)) {
                fleetInUse += thread.getRequestedFleetCount();
            }
        }
        return fleetMax - fleetInUse;
    }
}
