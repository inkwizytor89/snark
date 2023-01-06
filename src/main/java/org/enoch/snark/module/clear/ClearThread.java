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
        MessageDAO.getInstance().fetchAll().stream()
                .filter(entity -> entity.created.isBefore(LocalDateTime.now().minusDays(2)))
                .forEach(entity -> MessageDAO.getInstance().remove(entity));


        FleetDAO.getInstance().fetchAll().stream()
                .filter(entity -> entity.updated.isBefore(LocalDateTime.now().minusDays(2)))
                .forEach(entity -> FleetDAO.getInstance().remove(entity));
    }
}
