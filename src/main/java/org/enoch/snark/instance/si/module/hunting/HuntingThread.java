package org.enoch.snark.instance.si.module.hunting;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class HuntingThread extends AbstractThread {

    public static final String threadType = "hunting";
    public static final int UPDATE_TIME_IN_SECONDS = 10;

    private List<PlayerEntity> targets = new ArrayList<>();

//    public HuntingThread(ThreadMap map) {
//        super(map);
//    }

    @Override
    protected String getThreadType() {
        return threadType;
    }

    @Override
    protected String defaultPause() {
        return UPDATE_TIME_IN_SECONDS*10 + "S";
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
        return false;
//        if(DateUtil.isExpired(HIGH_SCORE, 23, ChronoUnit.HOURS) && noWaitingElementsByTag(HIGH_SCORE)) {
//        if(DateUtil.isExpired(HIGH_SCORE, 5, ChronoUnit.MINUTES)) {
//            if (consumer.noBlockingHashInQueue(HIGH_SCORE)) {
//                Integer highScorePages = Instance.getGlobalMainConfigMap().getConfigInteger(HIGH_SCORE_PAGES, 2);
//                new UpdateHighScoreCommand(highScorePages).push();
//            }
//            return false;
//        }
//        return true;
    }

}
