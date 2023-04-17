package org.enoch.snark.instance;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.instance.commander.Navigator;
import org.enoch.snark.module.AbstractThread;
import org.enoch.snark.module.building.BuildingThread;
import org.enoch.snark.module.collector.CollectorThread;
import org.enoch.snark.module.defense.DefenseThread;
import org.enoch.snark.module.expedition.ExpeditionThread;
import org.enoch.snark.module.farm.FarmThread;
import org.enoch.snark.module.fleetSave.FleetSaveThread;
import org.enoch.snark.module.scan.ScanThread;
import org.enoch.snark.module.space.SpaceThread;
import org.enoch.snark.module.update.UpdateThread;

import java.util.ArrayList;
import java.util.List;

public class BaseSI {
    private static BaseSI INSTANCE;

    private final List<AbstractThread> operationThreads = new ArrayList<>();

    private BaseSI() {
        operationThreads.add(new UpdateThread());
        operationThreads.add(new DefenseThread());
        operationThreads.add(new FleetSaveThread());
        operationThreads.add(new ExpeditionThread());
        operationThreads.add(new BuildingThread());
        operationThreads.add(new SpaceThread()); // explore space
        operationThreads.add(new ScanThread()); // checking i-player on defence
        operationThreads.add(new FarmThread()); // in progress
        operationThreads.add(new CollectorThread()); // in progress

        while(Navigator.getInstance().getEventFleetList() == null) SleepUtil.pause();
        operationThreads.forEach(Thread::start);
    }

    public static BaseSI getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new BaseSI();
        }
        return INSTANCE;
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
