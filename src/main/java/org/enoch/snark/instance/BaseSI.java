package org.enoch.snark.instance;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.module.AbstractThread;
import org.enoch.snark.module.building.BuildingThread;
import org.enoch.snark.module.clear.ClearThread;
import org.enoch.snark.module.collector.CollectorThread;
import org.enoch.snark.module.expedition.ExpeditionThread;
import org.enoch.snark.module.explore.SpaceThread;
import org.enoch.snark.module.farm.FarmThread;
import org.enoch.snark.module.fleetSave.FleetSaveThread;
import org.enoch.snark.module.scan.ScanThread;
import org.enoch.snark.module.update.UpdateThread;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BaseSI {
    private static BaseSI INSTANCE;

    private final List<AbstractThread> operationThreads = new ArrayList<>();
    private final List<AbstractThread> baseThreads = new ArrayList<>();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

    private BaseSI() {
        baseThreads.add(new UpdateThread());
        baseThreads.add(new ClearThread());
        operationThreads.add(new ExpeditionThread());
        operationThreads.add(new BuildingThread());
        operationThreads.add(new SpaceThread()); // explore space
        operationThreads.add(new ScanThread()); // checking i-player on defence
        operationThreads.add(new FarmThread()); // in progress
        operationThreads.add(new FleetSaveThread()); // in progress
        operationThreads.add(new CollectorThread()); // in progress
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

            operationThreads.forEach(Thread::start);

            while(true) {
                try {
                    Instance.updateConfig();
                    for (AbstractThread thread : operationThreads) {
                        boolean running = thread.isRunning();
                        boolean isModeOn = isModeOn(thread);
                        if ((!running && isModeOn) || (running && !isModeOn)) {
                            System.err.println("Thread " + thread.getThreadName() + (running ? " stop" : " start"));
                            thread.setRunning(!running);
                        }
                    }
                    SleepUtil.secondsToSleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(task).start();
    }

    public boolean isModeOn(AbstractThread thread) {
        String mode = Instance.config.mode;
        if(mode == null || mode.isEmpty())  return true;
        String[] configTerms = mode.split(",");
        for(String configTerm : configTerms) {
            if(configTerm.contains(thread.getThreadName())){
                String[] vars = configTerm.split("-");
                if(vars.length == 3) {
                    LocalTime start = LocalTime.parse(vars[1], dtf);
                    LocalTime end = LocalTime.parse(vars[2], dtf);
                    return (nowIsInMiddle(start, end) && start.isBefore(end)) ||
                            (!nowIsInMiddle(end, start) && start.isAfter(end));
                } else return true;
            }
        }
        return false;
    }

    private boolean nowIsInMiddle(LocalTime start, LocalTime end) {
        LocalTime now = LocalTime.now();
        return now.isAfter(start) && now.isBefore(end);
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
