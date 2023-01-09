package org.enoch.snark.instance;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.module.AbstractThread;
import org.enoch.snark.module.building.BuildingThread;
import org.enoch.snark.module.clear.ClearThread;
import org.enoch.snark.module.expedition.ExpeditionThread;
import org.enoch.snark.module.explore.SpaceThread;
import org.enoch.snark.module.farm.FarmThread;
import org.enoch.snark.module.scan.ScanThread;
import org.enoch.snark.module.update.UpdateThread;

import java.util.ArrayList;
import java.util.List;

public class BaseSI {
    private static BaseSI INSTANCE;

    private final List<AbstractThread> operationThreads = new ArrayList<>();
    private final List<AbstractThread> baseThreads = new ArrayList<>();

    private BaseSI() {
        baseThreads.add(new UpdateThread());
        baseThreads.add(new ClearThread());
        operationThreads.add(new ExpeditionThread());
        operationThreads.add(new BuildingThread());
        operationThreads.add(new SpaceThread()); // explore space
        operationThreads.add(new ScanThread()); // checking i-player on defence
        operationThreads.add(new FarmThread()); // in progress
    }

    public static BaseSI getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new BaseSI();
        }
        return INSTANCE;
    }

    public void run() {
        Runnable task = () -> {
            baseThreads.forEach(Thread::start);

            Commander commander = Commander.getInstance();
            while(commander.getFleetCount() <1) SleepUtil.pause();

            operationThreads.stream()
//                    .filter(this::isModeOn)
                    .forEach(Thread::start);

            while(true) {
                Instance.updateConfig();
                for (AbstractThread thread : operationThreads) {
                    boolean running = thread.isRunning();
                    if ((!running && isModeOn(thread)) || (running && !isModeOn(thread))) {
                        System.err.println("Thread "+thread.getThreadName() + (running ? " stop":" start"));
                        thread.setRunning(!running);
                    }
                }
                SleepUtil.secondsToSleep(10);
            }
        };
        new Thread(task).start();
    }

    public boolean isModeOn(AbstractThread thread) {
        String mode = Instance.config.mode;
        if(thread.getThreadName().contains(UpdateThread.threadName) ||
                thread.getThreadName().contains(ClearThread.threadName))
            return true;

        return mode == null || mode.isEmpty() || mode.contains(thread.getThreadName());
    }

    public int getAvailableFleetCount() {

        int fleetMax = Instance.commander.getFleetMax();
        if(fleetMax == 0) return 0;

        int fleetInUse = 0;
        for(AbstractThread thread : operationThreads) {
            if(thread.isRunning()) {
                fleetInUse += thread.getRequestedFleetCount();
            }
        }

        return fleetMax - fleetInUse;
    }
}
