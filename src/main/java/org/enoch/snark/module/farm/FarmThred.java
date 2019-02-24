package org.enoch.snark.module.farm;

import org.enoch.snark.db.dao.FarmDAO;
import org.enoch.snark.db.dao.impl.FarmDAOImpl;
import org.enoch.snark.instance.SI;
import org.enoch.snark.module.AbstractThred;

public class FarmThred extends AbstractThred {

    private final FarmDAOImpl farmDAO;

    public FarmThred(SI si) {
        super(si);
        farmDAO = new FarmDAOImpl(si.getInstance().universeEntity);
    }

    @Override
    public void run() {
        super.run();

//        farmDAO.getLastState();
        // wczytaj ostatni stan

        exploreSpace();

        int fleetNum = si.getAvailableFleetCount(this);
    }

    private void exploreSpace() {
        // wysyłamy sondy
    }

}
