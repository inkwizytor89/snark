package org.enoch.snark.module.clear;

import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.MessageDAO;
import org.enoch.snark.module.AbstractThread;

import java.time.LocalDateTime;

public class ClearThread extends AbstractThread {

    public static final String threadName = "clear";
    public static final int UPDATE_TIME_IN_MINUTES = 500;


    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return UPDATE_TIME_IN_MINUTES;
    }

    @Override
    protected void onStep() {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        MessageDAO.getInstance().clean(from);
        FleetDAO.getInstance().clean(from);
    }
}
