package org.enoch.snark.module.hunting;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.gi.command.impl.UpdateHighScoreCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.module.AbstractThread;
import org.enoch.snark.module.ConfigMap;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.enoch.snark.db.entity.CacheEntryEntity.HIGH_SCORE;
import static org.enoch.snark.instance.config.Config.HIGH_SCORE_PAGES;
import static org.enoch.snark.instance.config.Config.MAIN;

public class HuntingThread extends AbstractThread {

    public static final String threadName = "hunting";
    public static final int UPDATE_TIME_IN_SECONDS = 10;

    private List<PlayerEntity> targets = new ArrayList<>();

    public HuntingThread(ConfigMap map) {
        super(map);
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return UPDATE_TIME_IN_SECONDS*10;
    }

    @Override
    public int getRequestedFleetCount() {
        return 0;
    }

    @Override
    protected void onStep() {
        if(!updateHighScore()) return;

        // targets = get player list from statistic - this that have been processed (use db cache for that)

        // if player is active and is not scaned from 4 hour then scan him
        // if not check activity (if is active then check after 16 min if its not other scan)
    }

    private boolean updateHighScore() {
//        if(DateUtil.isExpired(HIGH_SCORE, 23, ChronoUnit.HOURS) && noWaitingElementsByTag(HIGH_SCORE)) {
        if(DateUtil.isExpired(HIGH_SCORE, 5, ChronoUnit.MINUTES)) {
            if (noWaitingElementsByTag(HIGH_SCORE)) {
                Integer highScorePages = Instance.config.getConfigInteger(MAIN, HIGH_SCORE_PAGES, 2);
                commander.push(new UpdateHighScoreCommand(highScorePages));
            }
            return false;
        }
        return true;
    }

}
