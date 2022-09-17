package org.enoch.snark.module.explore;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.dao.GalaxyDAO;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.gi.command.impl.GalaxyAnalyzeCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.SI;
import org.enoch.snark.model.Universe;
import org.enoch.snark.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Logger;

public class SpaceThread extends AbstractThread {

    public static final String threadName = "space";
    protected static final Logger LOG = Logger.getLogger( SpaceThread.class.getName());
    public static final int DATA_COUNT = 50;
    private final Instance instance;
    private final Queue<GalaxyEntity> notExplored = new PriorityQueue<>();
    private List<GalaxyEntity> galaxyToView = new ArrayList<>();

    public SpaceThread(SI si) {
        super(si);
        instance = si.getInstance();
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return pause;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if( GalaxyDAO.getInstance().fetchAll().isEmpty()) {
            int galaxyMax = Integer.parseInt(instance.universe.getConfig((Universe.GALAXY_MAX)));
            int systemMax = Integer.parseInt(instance.universe.getConfig((Universe.SYSTEM_MAX)));
            GalaxyDAO.getInstance().persistGalaxyMap(galaxyMax, systemMax);
        }
        notExplored.addAll(GalaxyDAO.getInstance().findNotExplored());
    }

    @Override
    protected void onStep() {
        int timeToBack = 71;
        pause = 300;
        if(LocalDateTime.now().getHour() < 5) {
            timeToBack = 23;
            pause = 60;
        }
        if(!notExplored.isEmpty()) {
            LOG.info(threadName+" still galaxy to look at "+notExplored.size());
            for (int i = 0; i < DATA_COUNT; i++) {
                GalaxyEntity poll = notExplored.poll();
                if(poll != null) {
                    instance.push(new GalaxyAnalyzeCommand(poll));
                }
            }
            return;
        }
        if (galaxyToView.isEmpty()) {
            galaxyToView = GalaxyDAO.getInstance().findLatestGalaxyToView(LocalDateTime.now().minusHours(timeToBack));
        }
        LOG.info("Potential galaxy to view " + galaxyToView.size());
        List<GalaxyEntity> toView = new ArrayList<>();
        if(galaxyToView.size() <= DATA_COUNT) {
            toView.addAll(galaxyToView);
        } else {
            toView.addAll(galaxyToView.subList(0,DATA_COUNT));
        }

        toView.forEach(galaxyEntity -> galaxyToView.remove(galaxyEntity));
        toView.forEach(galaxy -> instance.push(new GalaxyAnalyzeCommand(galaxy)));
    }
}
