package org.enoch.snark.instance;

import org.enoch.snark.module.AbstractThread;
import org.enoch.snark.module.explore.SpaceThread;
import org.enoch.snark.module.farm.FarmThread;
import org.enoch.snark.module.scan.ScanThread;

public class ResourceSI implements SI {

    public Instance instance;
    private SpaceThread spaceThred;
    private ScanThread scanThred;
    private FarmThread farmThred;

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
