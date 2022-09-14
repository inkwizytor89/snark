package org.enoch.snark.module.scan;

import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.SI;
import org.enoch.snark.module.AbstractThread;

import java.util.Optional;

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
        return 60;
    }

    @Override
    protected void onStep() {
        Optional<TargetEntity> target = TargetDAO.getInstance().findNotScaned();
        if(target.isPresent()) {
            FleetEntity fleet = FleetEntity.createSpyFleet(instance, target.get());
            Instance.getInstance().commander.push(new SendFleetCommand(fleet));
//            FleetDAO.getInstance().saveOrUpdate(fleet);
        }
    }
}
