package org.enoch.snark.module.explore;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.command.impl.GalaxyAnalyzeCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.SI;
import org.enoch.snark.model.SystemView;
import org.enoch.snark.module.AbstractThread;

import java.util.*;

public class SpaceThread extends AbstractThread {

    public static final String threadName = "space";
    private final Instance instance;

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
        return 120;
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkMissing();
    }

    @Override
    protected void onStep() {
        final Optional<GalaxyEntity> latestGalaxyToView = instance.daoFactory.galaxyDAO.findLatestGalaxyToView();

        if(!latestGalaxyToView.isPresent()) {
            System.err.println(SpaceThread.class.getName()+": Database doesn't contains "+GalaxyEntity.class.getName());
        } else {
            if(DateUtil.lessThanDays(4, latestGalaxyToView.get().updated)) {
                System.err.println(SpaceThread.class.getName()+
                        ": No new galaxy to scan[oldest is from "+latestGalaxyToView.get().updated+"]");
            } else {
                instance.commander.push(new GalaxyAnalyzeCommand(instance, latestGalaxyToView.get().toSystemView()));
            }
        }
    }

    private void checkMissing() {
        Map<SystemView, GalaxyEntity> spaceMap = buildSpaceMap();
        spaceMap.entrySet().stream()
                .filter(entry -> entry.getValue() == null)
                .forEach(entry -> instance.commander.push(new GalaxyAnalyzeCommand(instance, entry.getKey())));
    }

    private Map<SystemView, GalaxyEntity> buildSpaceMap() {
        List<SystemView> toView = new LinkedList<>();
        for(ColonyEntity source : instance.sources) {
            toView.addAll(generateSystemToView(source));
        }
        Map<SystemView, GalaxyEntity> spaceMap= new HashMap<>();
        toView.forEach(view -> spaceMap.put(view, null));

        instance.daoFactory.galaxyDAO.fetchAll().stream()
                .filter(galaxy -> galaxy.universe.equals(instance.universeEntity))
                .forEach(galaxyEntity -> spaceMap.put(galaxyEntity.toSystemView(), galaxyEntity));
        return spaceMap;
    }

    private Collection<SystemView> generateSystemToView(ColonyEntity source) {
        List<SystemView> result = new ArrayList<>();
        int base = instance.universeEntity.systemMax;
        int start = ((base + source.system - instance.universeEntity.explorationArea) % base) +1;
        for(int i = 0; i < 2*instance.universeEntity.explorationArea+2; i++ ) {
            result.add(new SystemView(source.galaxy, ((start+i)%base)+1));
        }
        return result;
    }
}
