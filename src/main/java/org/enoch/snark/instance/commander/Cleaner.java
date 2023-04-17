package org.enoch.snark.instance.commander;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.MessageDAO;

import java.time.LocalDateTime;

public class Cleaner {

    private static Cleaner INSTANCE;

    private Cleaner() {
        startRefreshingConfig();
    }

    public static Cleaner getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Cleaner();
        }
        return INSTANCE;
    }

    public void startRefreshingConfig() {
        Runnable task = () -> {

            while(true) {
                try {
                    SleepUtil.secondsToSleep(2000);
                    LocalDateTime from = LocalDateTime.now().minusDays(1);
                    MessageDAO.getInstance().clean(from);
                    FleetDAO.getInstance().clean(from);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(task).start();
    }
}
