package org.enoch.snark.instance.si.module.space;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.GalaxyDAO;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.gi.command.impl.GalaxyAnalyzeCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.to.SystemView;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ConfigMap;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Logger;

import static org.enoch.snark.instance.si.module.ConfigMap.GALAXY_MAX;
import static org.enoch.snark.instance.si.module.ConfigMap.SYSTEM_MAX;

public class SpaceThread extends AbstractThread {

    public static final String threadName = "space";
    protected static final Logger LOG = Logger.getLogger( SpaceThread.class.getName());
    public static final int DATA_COUNT = 10;
    private final Queue<GalaxyEntity> notExplored = new PriorityQueue<>();
    private List<GalaxyEntity> galaxyToView = new ArrayList<>();

    public SpaceThread(ConfigMap map) {
        super(map);
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
        int galaxyMax = Instance.getMainConfigMap().getConfigInteger(GALAXY_MAX, 6);
        int systemMax = Instance.getMainConfigMap().getConfigInteger(SYSTEM_MAX, 499);
        for(int i = 1 ; i <= galaxyMax; i++) {
            if(isNecessaryGalaxyPersist(i))
                GalaxyDAO.getInstance().persistGalaxyMap(i, systemMax);
        }
        notExplored.addAll(GalaxyDAO.getInstance().findNotExplored());
    }

    private boolean isNecessaryGalaxyPersist(final Integer galaxy) {
        return ColonyDAO.getInstance().fetchAll().stream().anyMatch(colony -> colony.galaxy.equals(galaxy)) &&
                !GalaxyDAO.getInstance().find(new SystemView(galaxy, 1)).isPresent();
    }

    @Override
    protected void onStep() {
        if (!commander.notingToPool()) return;
        int timeToBack = 95;
        pause = 300;
        if(LocalDateTime.now().getHour() < 5) {
            timeToBack = 23;
            pause = 90;
        }
        if(!notExplored.isEmpty()) {
//            LOG.info(threadName+" still galaxy to look at "+notExplored.size());
            for (int i = 0; i < DATA_COUNT; i++) {
                GalaxyEntity poll = notExplored.poll();
                if(poll != null) {
                    new GalaxyAnalyzeCommand(poll).push();
                }
            }
            return;
        }
        if (galaxyToView.isEmpty()) {
            galaxyToView = GalaxyDAO.getInstance().findLatestGalaxyToView(LocalDateTime.now().minusHours(timeToBack));
        }
        if (galaxyToView.isEmpty()) {
            return;
        }
        LOG.info("Potential galaxy to view " + galaxyToView.size());
        List<GalaxyEntity> toView = new ArrayList<>();
        if(galaxyToView.size() <= DATA_COUNT) {
            toView.addAll(galaxyToView);
        } else {
            toView.addAll(galaxyToView.subList(0,DATA_COUNT));
        }

        toView.forEach(galaxyEntity -> galaxyToView.remove(galaxyEntity));
        toView.forEach(galaxy -> new GalaxyAnalyzeCommand(galaxy).push());
    }
}
