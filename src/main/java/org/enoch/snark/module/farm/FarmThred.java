package org.enoch.snark.module.farm;

import org.enoch.snark.instance.SI;
import org.enoch.snark.module.AbstractThred;

public class FarmThred extends AbstractThred {
    public FarmThred(SI si) {
        super(si);
    }

    @Override
    public void run() {
        super.run();

        exploreSpace();

        int fleetNum = si.getAvailableFleetCount(this);
    }

    private void exploreSpace() {
        // wysyłamy sondy
    }

    private void
}
