package org.enoch.snark.module.regular;

import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.MessageDAO;
import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.module.AbstractThread;

import java.time.LocalDateTime;

import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_RESEARCH;

public class RegularThread extends AbstractThread {

    public static final String threadName = "regular";
    public static final int UPDATE_TIME_IN_SECONDS = 500;

    public RegularThread() {
        super();
        setRunning(true);
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return UPDATE_TIME_IN_SECONDS;
    }

    @Override
    protected void onStep() {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        MessageDAO.getInstance().clean(from);
        FleetDAO.getInstance().clean(from);

        PlayerEntity mainPlayer = PlayerDAO.getInstance().fetch(PlayerEntity.mainPlayer());
        if(LocalDateTime.now().minusDays(7L).isAfter(mainPlayer.updated)) {
            commander.push(new OpenPageCommand(PAGE_RESEARCH, mainPlayer));
        }
    }
}
