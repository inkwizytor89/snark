package org.enoch.snark.module.scan;

import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.SI;
import org.enoch.snark.module.AbstractThread;

import java.util.List;

public class ScanThread extends AbstractThread {

    public static final String threadName = "scan";
    private final Instance instance;

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
        return 300;
    }

    @Override
    protected void onStep() {
        List<TargetEntity> targets = TargetDAO.getInstance().findNotScanned(10);
        if(!targets.isEmpty()) {
            for(TargetEntity target : targets) {
                FleetEntity fleet = FleetEntity.createSpyFleet(instance, target);
                Instance.getInstance().commander.push(new SendFleetCommand(fleet));
            }
//            FleetDAO.getInstance().saveOrUpdate(fleet);
        }
    }
}
