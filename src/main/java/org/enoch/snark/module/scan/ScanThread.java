package org.enoch.snark.module.scan;

import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.SI;
import org.enoch.snark.module.AbstractThread;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ScanThread extends AbstractThread {

    public static final String threadName = "scan";
    private final Instance instance;
    private Queue<TargetEntity> notScanned = new LinkedList<>();

    public ScanThread(SI si) {
        super(si);
        instance = si.getInstance();
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return 180;
    }

    @Override
    protected void onStep() {
        if(notScanned.isEmpty()) {
            notScanned =  new LinkedList<>(TargetDAO.getInstance().findNotScanned());
        }
        for (int i = 0; i < 10; i++) {
            if(!notScanned.isEmpty()) {
                FleetEntity fleet = FleetEntity.createSpyFleet(notScanned.poll());
                Instance.getInstance().commander.push(new SendFleetCommand(fleet));
//            FleetDAO.getInstance().saveOrUpdate(fleet);
            }
        }
    }
}
