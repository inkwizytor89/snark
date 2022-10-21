package org.enoch.snark.module.scan;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.SI;
import org.enoch.snark.module.AbstractThread;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class ScanThread extends AbstractThread {

    public static final String threadName = "scan";
    protected static final Logger LOG = Logger.getLogger( ScanThread.class.getName());
    private Queue<TargetEntity> notScanned = new LinkedList<>();

    public ScanThread(SI si) {
        super(si);
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
    protected void onStart() {
        super.onStart();
        SleepUtil.secondsToSleep(10);
    }

    @Override
    protected void onStep() {
        pause = 400;
        if(notScanned.isEmpty()) {
            notScanned =  new LinkedList<>(TargetDAO.getInstance().findNotScanned());
        }
        if(notScanned.isEmpty()) {
            pause = 2400;
        }
        LOG.info(threadName+" still id "+notScanned.size());
        for (int i = 0; i < 10; i++) {
            if(!notScanned.isEmpty()) {
                FleetEntity fleet = FleetEntity.createSpyFleet(notScanned.poll());
                FleetDAO.getInstance().saveOrUpdate(fleet);
//                Instance.getInstance().push(new SendFleetCommand(fleet));
            }
        }
    }
}
