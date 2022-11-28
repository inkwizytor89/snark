package org.enoch.snark.instance;

import org.enoch.snark.module.AbstractThread;
import org.enoch.snark.module.building.BuildingThread;
import org.enoch.snark.module.clear.ClearThread;
import org.enoch.snark.module.expedition.ExpeditionThread;
import org.enoch.snark.module.explore.SpaceThread;
import org.enoch.snark.module.farm.FarmThread;
import org.enoch.snark.module.scan.ScanThread;
import org.enoch.snark.module.update.UpdateThread;

public class BaseSI implements SI {

    private final ClearThread clearThread;
    public Instance instance;
    private final UpdateThread updateThread;
    private final SpaceThread spaceThread;
    private final ScanThread scanThread;
    private final FarmThread farmThread;
    private final ExpeditionThread expeditionThread;
    private final BuildingThread buildingThread;

    public BaseSI(Instance instance) {
        this.instance = instance;
        this.updateThread = new UpdateThread(this);
        this.clearThread = new ClearThread(this);
        this.expeditionThread = new ExpeditionThread(this);
        this.buildingThread = new BuildingThread(this);
        this.spaceThread = new SpaceThread(this); // explore space
        this.scanThread = new ScanThread(this); // checking i-player on defence
        this.farmThread = new FarmThread(this); // in progres
    }

    public void run() {
        new Thread(updateThread).start();
        new Thread(clearThread).start();

        if(isModeOn(ExpeditionThread.threadName)) {
            new Thread(expeditionThread).start();
        }
        if(isModeOn(BuildingThread.threadName)) {
            new Thread(buildingThread).start();
        }
        if(isModeOn(SpaceThread.threadName)) {
            new Thread(spaceThread).start();
        }
        if(isModeOn(ScanThread.threadName)) {
            new Thread(scanThread).start();
        }
        if(isModeOn(FarmThread.threadName)) {
            new Thread(farmThread).start();
        }
    }

    public boolean isModeOn(String threadName) {
        String mode = Instance.universe.mode;
        return mode == null || mode.isEmpty() || mode.contains(threadName);
    }

    @Override
    public int getAvailableFleetCount(AbstractThread thread) {

        if(thread instanceof FarmThread) {
            return Instance.commander.getFleetMax() - Instance.commander.getExpeditionMax() - 2;
        }
        return 0;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }
}
