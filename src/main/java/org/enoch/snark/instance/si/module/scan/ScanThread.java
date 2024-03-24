package org.enoch.snark.instance.si.module.scan;

import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ConfigMap;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class ScanThread extends AbstractThread {

    protected static final Logger LOG = Logger.getLogger(ScanThread.class.getName());
    public static final String threadName = "scan";

    private Queue<TargetEntity> notScanned = new LinkedList<>();

    public ScanThread(ConfigMap map) {
        super(map);
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return pause;
    }

    @Override
    public int getRequestedFleetCount() {
        return 1;
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStep() {
        if(!loadNotScannedTargets()) {
            pause = 600;
            return;
        }
        if(commander.notingToPool()) {
            setWaitingScan();
        }
    }

    private boolean loadNotScannedTargets() {
        pause = 150;
        if(notScanned.isEmpty()) {
            notScanned = new LinkedList<>(targetDAO.findNotScanned());
        }
        return !notScanned.isEmpty();
    }

    private void setWaitingScan() {
        LOG.info(threadName + " still to scan " + notScanned.size());
        for (int i = 0; i < 10; i++) {
            if (!notScanned.isEmpty()) {
                FleetEntity fleet = FleetEntity.createSpyFleet(notScanned.poll());
                fleetDAO.saveOrUpdate(fleet);
            }
        }
    }
}
