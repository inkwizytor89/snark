package org.enoch.snark.instance;

import org.enoch.snark.module.AbstractThread;
import org.enoch.snark.module.building.BuildingThread;
import org.enoch.snark.module.expedition.ExpeditionThread;
import org.enoch.snark.module.explore.SpaceThread;
import org.enoch.snark.module.farm.FarmThread;
import org.enoch.snark.module.scan.ScanThread;
import org.enoch.snark.module.update.UpdateThread;

public class BaseSI implements SI {

    public Instance instance;
    private UpdateThread updateThread;
    private SpaceThread spaceThred;
    private ScanThread scanThred;
    private FarmThread farmThred;
    private ExpeditionThread expeditionThred;
    private BuildingThread buildingThread;

    public BaseSI(Instance instance) {
        this.instance = instance;
        this.updateThread = new UpdateThread(this);
        this.expeditionThred = new ExpeditionThread(this);
        this.buildingThread = new BuildingThread(this);
        this.spaceThred = new SpaceThread(this); // explore space
        this.scanThred = new ScanThread(this); // checking i-player on defence
        this.farmThred = new FarmThread(this); // in progres
    }

    public void run() {
        new Thread(updateThread).start();

        if(isModeOn(ExpeditionThread.threadName)) {
            new Thread(expeditionThred).start();
        }
        if(isModeOn(BuildingThread.threadName)) {
            new Thread(buildingThread).start();
        }
        if(isModeOn(SpaceThread.threadName)) {
            new Thread(spaceThred).start();
        }
        if(isModeOn(ScanThread.threadName)) {
            new Thread(scanThred).start();
        }
        if(isModeOn(FarmThread.threadName)) {
            new Thread(farmThred).start();
        }
    }

    public boolean isModeOn(String threadName) {
        String mode = instance.universe.mode;
        return mode == null || mode.isEmpty() || mode.contains(threadName);
    }

    @Override
    public int getAvailableFleetCount(AbstractThread thred) {

        if(thred instanceof FarmThread) {
            return 4;//instance.commander.getFleetMax() - 2;
        }
        return 0;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }
}
