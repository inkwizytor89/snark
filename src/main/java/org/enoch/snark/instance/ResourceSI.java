package org.enoch.snark.instance;

import org.enoch.snark.db.dao.impl.AbstractDAOImpl;
import org.enoch.snark.module.AbstractThred;
import org.enoch.snark.module.farm.FarmThred;

public class ResourceSI implements SI {

    public Instance instance;
    public final FarmThred farmThred;

    public ResourceSI(Instance instance) {
        this.instance = instance;
        farmThred = new FarmThred(this);


        farmThred.run();
    }

    public void run() {
    }

    @Override
    public int getAvailableFleetCount(AbstractThred thred) {

        if(thred instanceof FarmThred) {
            return instance.commander.getFleetMax() - 2;
        }
        return 0;
    }
}
