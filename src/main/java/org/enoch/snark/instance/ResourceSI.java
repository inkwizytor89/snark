package org.enoch.snark.instance;

import org.enoch.snark.gi.command.impl.ReadMessageCommand;
import org.enoch.snark.module.AbstractThred;
import org.enoch.snark.module.explore.SpaceThred;
import org.enoch.snark.module.farm.FarmThred;
import org.enoch.snark.module.scan.ScanThred;

public class ResourceSI implements SI {

    public Instance instance;
    private SpaceThred spaceThred;
    private ScanThred scanThred;
    private FarmThred farmThred;

    public ResourceSI(Instance instance) {
        this.instance = instance;
        spaceThred = new SpaceThred(this);
        scanThred = new ScanThred(this);
        farmThred = new FarmThred(this);
    }

    public void run() {
        instance.commander.push(new ReadMessageCommand(instance));
//        new Thread(spaceThred).start();
//        new Thread(scanThred).start();
//        new Thread(farmThred).start();
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
