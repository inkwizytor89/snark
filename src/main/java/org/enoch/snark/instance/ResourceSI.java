package org.enoch.snark.instance;

import org.enoch.snark.db.dao.impl.AbstractDAOImpl;
import org.enoch.snark.module.AbstractThred;
import org.enoch.snark.module.explore.SpaceThred;
import org.enoch.snark.module.farm.FarmThred;
import org.enoch.snark.module.scan.ScanThred;

import static com.google.common.collect.ComparisonChain.start;

public class ResourceSI implements SI {

    private final SpaceThred spaceThred;
    private final ScanThred scanThred;
    public Instance instance;
    public final FarmThred farmThred;

    public ResourceSI(Instance instance) {
        this.instance = instance;
        spaceThred = new SpaceThred(this);
        scanThred = new ScanThred(this);
        farmThred = new FarmThred(this);
    }

    public void run() {
        new Thread(spaceThred).start();
        new Thread(scanThred).start();
        new Thread(farmThred).start();
    }

    @Override
    public int getAvailableFleetCount(AbstractThred thred) {

        if(thred instanceof FarmThred) {
            return instance.commander.getFleetMax() - 2;
        }
        return 0;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }
}
