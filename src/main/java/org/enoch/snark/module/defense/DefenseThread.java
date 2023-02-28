package org.enoch.snark.module.defense;

import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.MessageDAO;
import org.enoch.snark.instance.commander.Navigator;
import org.enoch.snark.model.EventFleet;
import org.enoch.snark.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DefenseThread extends AbstractThread {

    public static final String threadName = "defense";
    public static final int UPDATE_TIME_IN_SECONDS = 5;

    public DefenseThread() {
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
    public int getRequestedFleetCount() {
        return 1;
    }

    @Override
    protected void onStep() {
        if(isDefenseOff()) return;

        // commander powinnien trigerowac update floty jesli jest znak under attack

        List<EventFleet> eventFleetList = Navigator.getInstance().getEventFleetList();

        if(!isUnderAttack()) {
            clearCache();
            return;
        }

        playMusic();

        List<EventFleet> moreThan4min = isMoreThan4min();
        if(!moreThan4min.isEmpty()) return; //write to player

        List<EventFleet> lessThan4min = isLessThan4min();
        if(!lessThan4min.isEmpty()) return; //escape fleet

    }

    private List<EventFleet> isLessThan4min() {
        return new ArrayList<>();
    }

    private List<EventFleet> isMoreThan4min() {
        return new ArrayList<>();
    }

    private void playMusic() {

    }

    private void clearCache() {
        return ;
    }

    private boolean isUnderAttack() {
        return isAttack() || isAgressiveSpy() || isDestroyMoonFleet();
    }

    private boolean isDestroyMoonFleet() {
        return false;
    }

    private boolean isAgressiveSpy() {
        return false;
    }

    private boolean isAttack() {
        return false;
    }

    private boolean isDefenseOff() {
        return true;
    }
}
