package org.enoch.snark.instance.si.module.scan;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.db.entity.FleetEntity;

import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.db.repository.ConfigRepository;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class ScanThread extends AbstractThread {

    protected static final Logger LOG = Logger.getLogger(ScanThread.class.getName());
    public static final String threadType = ScanDefinition.THREAD_TYPE;
    private final ConfigRepository configRepository;
    private int threadPause = 150;

    private Queue<TargetEntity> notScanned = new LinkedList<>();
    private ScanDefinition config;

    @Override
    protected void onStart() {
        super.onStart();
        config = new ScanDefinition();
        config.update(configRepository.findByThread_Module_NameAndThread_Name(
                threadEntity.module.name, "main"));
        config.update(threadEntity.configs);
    }

    @Override
    protected void onStep() {
        if(!loadNotScannedTargets()) {
            threadPause = 600;
            return;
        }
        throw new NotImplementedException("To fix in spring version");
//        if(consumer.notingToPool()) {
//            setWaitingScan();
//        }
    }

    @Override
    public ThreadDefinition threadConfiguration() {
        return config.threadConfiguration();
    }

    private boolean loadNotScannedTargets() {
        threadPause = 150;
        if(notScanned.isEmpty()) {
//            notScanned = new LinkedList<>(targetDAO.findNotScanned());
        }
        return !notScanned.isEmpty();
    }

    private void setWaitingScan() {
        LOG.info(threadType + " still to scan " + notScanned.size());
        for (int i = 0; i < 10; i++) {
            if (!notScanned.isEmpty()) {
                FleetEntity fleet = FleetEntity.createSpyFleet(notScanned.poll());
//                fleetDAO.saveOrUpdate(fleet);
            }
        }
    }
}
