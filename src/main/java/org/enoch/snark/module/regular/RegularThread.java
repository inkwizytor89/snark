package org.enoch.snark.module.regular;

import org.enoch.snark.module.AbstractThread;

public class RegularThread extends AbstractThread {

    public static final String threadName = "regular";
    public static final int UPDATE_TIME_IN_SECONDS = 500;

    @Override
    protected void onStart() {
        super.onStart();
        setAutoRunning(true);
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
//        LocalDateTime from = LocalDateTime.now().minusDays(1);
//        MessageDAO.getInstance().clean(from);
//        FleetDAO.getInstance().clean(from);
//
//        PlayerEntity mainPlayer = PlayerDAO.getInstance().fetch(PlayerEntity.mainPlayer());
//        if(LocalDateTime.now().minusDays(7L).isAfter(mainPlayer.updated)) {
//            commander.push(new OpenPageCommand(PAGE_RESEARCH, mainPlayer));
//        }
    }
}
