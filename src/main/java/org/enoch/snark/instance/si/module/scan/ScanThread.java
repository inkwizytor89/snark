package org.enoch.snark.instance.si.module.scan;

import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ThreadMap;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class ScanThread extends AbstractThread {

    protected static final Logger LOG = Logger.getLogger(ScanThread.class.getName());
    public static final String threadType = "scan";
    private int threadPause = 150;

    private Queue<TargetEntity> notScanned = new LinkedList<>();

    public ScanThread(ThreadMap map) {
        super(map);
    }

    @Override
    protected String getThreadType() {
        return threadType;
    }

    @Override
    protected int getPauseInSeconds() {
        return threadPause;
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
            threadPause = 600;
            return;
        }
        if(consumer.notingToPool()) {
            setWaitingScan();
        }
    }

    private boolean loadNotScannedTargets() {
        threadPause = 150;
        if(notScanned.isEmpty()) {
            notScanned = new LinkedList<>(targetDAO.findNotScanned());
        }
        return !notScanned.isEmpty();
    }

    private void setWaitingScan() {
        LOG.info(threadType + " still to scan " + notScanned.size());
        for (int i = 0; i < 10; i++) {
            if (!notScanned.isEmpty()) {
                FleetEntity fleet = FleetEntity.createSpyFleet(notScanned.poll());
                fleetDAO.saveOrUpdate(fleet);
            }
        }
    }
}
