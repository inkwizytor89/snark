package org.enoch.snark.instance;

import org.enoch.snark.module.AbstractThread;
import org.enoch.snark.module.building.BuildingThread;
import org.enoch.snark.module.expedition.ExpeditionThread;
import org.enoch.snark.module.explore.SpaceThread;
import org.enoch.snark.module.farm.FarmThread;
import org.enoch.snark.module.scan.ScanThread;

public class BaseSI implements SI {

    public Instance instance;
    private SpaceThread spaceThred;
    private ScanThread scanThred;
    private FarmThread farmThred;
    private ExpeditionThread expeditionThred;
    private BuildingThread buildingThread;

    public BaseSI(Instance instance) {
        this.instance = instance;
        this.expeditionThred = new ExpeditionThread(this);
        this.buildingThread = new BuildingThread(this);
        this.spaceThred = new SpaceThread(this); // explore space
        this.scanThred = new ScanThread(this); // checking i-player on defence
        this.farmThred = new FarmThread(this); // in progres
    }

    public void run() {
        String mode = instance.universe.mode;
        if(mode == null || mode.isEmpty() || mode.contains(ExpeditionThread.threadName)) {
            new Thread(expeditionThred).start();
        }
        if(mode == null || mode.isEmpty() || mode.contains(BuildingThread.threadName)) {
            new Thread(buildingThread).start();
        }
        if(mode == null || mode.isEmpty() || mode.contains(SpaceThread.threadName)) {
            new Thread(spaceThred).start();
        }
        if(mode == null || mode.isEmpty() || mode.contains(ScanThread.threadName)) {
            new Thread(scanThred).start();
        }
        if(mode == null || mode.isEmpty() || mode.contains(FarmThread.threadName)) {
            new Thread(farmThred).start();
        }
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
