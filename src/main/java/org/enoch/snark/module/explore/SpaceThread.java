package org.enoch.snark.module.explore;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.dao.GalaxyDAO;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.gi.command.impl.GalaxyAnalyzeCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.SI;
import org.enoch.snark.model.Universe;
import org.enoch.snark.module.AbstractThread;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class SpaceThread extends AbstractThread {

    public static final String threadName = "space";
    public static final int DATA_COUNT = 50;
    private final Instance instance;
    private final Queue<GalaxyEntity> notExplored = new PriorityQueue<>();

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
        return 600;
    }

    @Override
    protected void onStart() {
        super.onStart();
        List<GalaxyEntity> latestGalaxyToView = GalaxyDAO.getInstance().findLatestGalaxyToView(DATA_COUNT);
        if(latestGalaxyToView.isEmpty()) {
            int galaxyMax = Integer.parseInt(instance.universe.getConfig((Universe.GALAXY_MAX)));
            int systemMax = Integer.parseInt(instance.universe.getConfig((Universe.SYSTEM_MAX)));
            GalaxyDAO.getInstance().persistGalaxyMap(galaxyMax, systemMax);
        }
        notExplored.addAll(GalaxyDAO.getInstance().findNotExplored());
    }

    @Override
    protected void onStep() {
        if(!notExplored.isEmpty()) {
            for (int i = 0; i < DATA_COUNT; i++) {
                GalaxyEntity poll = notExplored.poll();
                if(poll != null) {
                    instance.commander.push(new GalaxyAnalyzeCommand(poll));
                }
            }
            return;
        }
        GalaxyDAO.getInstance().findLatestGalaxyToView(DATA_COUNT).stream()
                .filter(galaxyEntity -> !DateUtil.lessThanHours(22, galaxyEntity.updated))
                .forEach(galaxy -> instance.commander.push(new GalaxyAnalyzeCommand(galaxy)));
    }
}
