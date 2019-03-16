package org.enoch.snark.module.scan;

import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.SI;
import org.enoch.snark.module.AbstractThred;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ScanThred extends AbstractThred {

    private final Instance instance;

    public ScanThred(SI si) {
        super(si);
        instance = si.getInstance();
    }

    @Override
    public void run() {
        super.run();
        while(true) {
            Optional<TargetEntity> target = instance.daoFactory.targetDAO.findNotScaned();
            if(target.isPresent()) {
                FleetEntity fleet = FleetEntity.createSpyFleet(instance, target.get());
                instance.daoFactory.fleetDAO.saveOrUpdate(fleet);
            }
            instance.session.sleep(TimeUnit.SECONDS, 30);
        }
    }
}
