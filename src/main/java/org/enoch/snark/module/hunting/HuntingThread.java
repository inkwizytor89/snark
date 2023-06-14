package org.enoch.snark.module.hunting;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.gi.command.impl.UpdateHighScoreCommand;
import org.enoch.snark.module.AbstractThread;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.enoch.snark.db.entity.CacheEntryEntity.HIGH_SCORE;

public class HuntingThread extends AbstractThread {

    public static final String threadName = "hunting";
    public static final int UPDATE_TIME_IN_SECONDS = 10;

    private List<PlayerEntity> targets = new ArrayList<>();

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
        return 0;
    }

    @Override
    protected void onStep() {
        updateHighScore();
    }

    private void updateHighScore() {
        if(DateUtil.isExpired(HIGH_SCORE, 23, ChronoUnit.HOURS) && noWaitingElementsByTag(HIGH_SCORE)) {
            commander.push(new UpdateHighScoreCommand());
        }
    }

}
