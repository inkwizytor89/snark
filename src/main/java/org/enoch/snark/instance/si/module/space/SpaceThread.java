package org.enoch.snark.instance.si.module.space;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.GalaxyDAO;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.gi.command.impl.GalaxyAnalyzeCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.QueueRunType;
import org.enoch.snark.instance.model.to.SystemView;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ConfigMap;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Logger;

import static org.enoch.snark.instance.si.module.ConfigMap.*;

public class SpaceThread extends AbstractThread {

    public static final String threadType = "space";
    protected static final Logger LOG = Logger.getLogger( SpaceThread.class.getName());
    public static final int DATA_COUNT = 10;
    private int threadPause = 300;

    private final Queue<GalaxyEntity> notExplored = new PriorityQueue<>();
    private List<GalaxyEntity> galaxyToView = new ArrayList<>();

    public SpaceThread(ConfigMap map) {
        super(map);
        threadPause = 300;
    }

    @Override
    protected String getThreadType() {
        return threadType;
    }

    @Override
    protected int getPauseInSeconds() {
        return threadPause;
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
        Integer pageSize = map.getConfigInteger(PAGE_SIZE, DATA_COUNT);
        if (!commander.notingToPool()) return;
        if(!notExplored.isEmpty()) {
            log("Never checked galaxy: "+notExplored.size()+" page="+pageSize);
            for (int i = 0; i < pageSize; i++) {
                GalaxyEntity poll = notExplored.poll();
                if(poll != null) {
                    new GalaxyAnalyzeCommand(poll).setRunType(QueueRunType.SPAM).push();
                }
            }
            return;
        }
        if (galaxyToView.isEmpty()) {
            galaxyToView = GalaxyDAO.getInstance().findLatestGalaxyToView(LocalDateTime.now().minusDays(5));
        }
        if (galaxyToView.isEmpty()) {
            return;
        }
        log("Outdated galaxy: "+galaxyToView.size()+" page="+pageSize);
        List<GalaxyEntity> toView = new ArrayList<>();
        if(galaxyToView.size() <= pageSize) {
            toView.addAll(galaxyToView);
        } else {
            toView.addAll(galaxyToView.subList(0,pageSize));
        }

        toView.forEach(galaxyEntity -> galaxyToView.remove(galaxyEntity));
        toView.forEach(galaxy -> new GalaxyAnalyzeCommand(galaxy).setRunType(QueueRunType.SPAM).push());
    }
}
