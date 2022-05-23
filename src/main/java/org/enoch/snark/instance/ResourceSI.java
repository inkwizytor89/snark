package org.enoch.snark.instance;

import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.command.impl.ReadMessageCommand;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.module.AbstractThred;
import org.enoch.snark.module.explore.SpaceThred;
import org.enoch.snark.module.farm.FarmThred;
import org.enoch.snark.module.scan.ScanThred;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ResourceSI implements SI {

    public Instance instance;
    private SpaceThred spaceThred;
    private ScanThred scanThred;
    private FarmThred farmThred;

    public ResourceSI(Instance instance) {
        this.instance = instance;
//        spaceThred = new SpaceThred(this); //przeglada przestrzeń
//        scanThred = new ScanThred(this); //szpieguje ...
//        farmThred = new FarmThred(this); // zbieranie z idkow surowcow
    }

    public void run() {
//        Optional<TargetEntity> targetEntity = instance.daoFactory.targetDAO.find(1, 266, 4);
//        FleetEntity farmFleet = FleetEntity.createFarmFleet(instance, targetEntity.get());
//        instance.daoFactory.fleetDAO.saveOrUpdate(farmFleet);
//        new Thread(spaceThred).start();
//        instance.session.sleep(TimeUnit.SECONDS, 2);
        new Thread(farmThred).start();
//        instance.session.sleep(TimeUnit.SECONDS, 1);
//        new Thread(scanThred).start();
    }

    @Override
    public int getAvailableFleetCount(AbstractThred thred) {

        if(thred instanceof FarmThred) {
            return 4;//instance.commander.getFleetMax() - 2;
        }
        return 0;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }
}
