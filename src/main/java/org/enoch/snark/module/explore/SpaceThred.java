package org.enoch.snark.module.explore;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.db.entity.SourceEntity;
import org.enoch.snark.gi.command.impl.GalaxyAnalyzeCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.SI;
import org.enoch.snark.model.SystemView;
import org.enoch.snark.module.AbstractThred;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SpaceThred extends AbstractThred {

    private final Instance instance;

    public SpaceThred(SI si) {
        super(si);
        instance = si.getInstance();
    }

    @Override
    public void run() {
        super.run();
        checkMissing();
        while(true) {

            final Optional<GalaxyEntity> latestGalaxyToView = instance.daoFactory.galaxyDAO.findLatestGalaxyToView();

            if(!latestGalaxyToView.isPresent()) {
                System.err.println(SpaceThred.class.getName()+": Database doesn't contains "+GalaxyEntity.class.getName());
            } else {
                if(DateUtil.lessThan20H(latestGalaxyToView.get().updated)) {
                    System.err.println(SpaceThred.class.getName()+": No new galaxy to scan");
                } else {
                    instance.commander.push(new GalaxyAnalyzeCommand(instance, latestGalaxyToView.get().toSystemView()));
                }
            }
            instance.session.sleep(TimeUnit.SECONDS, 30);
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
        for(SourceEntity source : instance.universeEntity.sources) {
            toView.addAll(generateSystemToView(source));
        }
        Map<SystemView, GalaxyEntity> spaceMap= new HashMap<>();
        toView.forEach(view -> spaceMap.put(view, null));
        instance.daoFactory.galaxyDAO.fetchAll().forEach(galaxyEntity ->
                spaceMap.put(galaxyEntity.toSystemView(), galaxyEntity));
        return spaceMap;
    }

    private Collection<SystemView> generateSystemToView(SourceEntity source) {
        List<SystemView> result = new ArrayList<>();
        int base = instance.universeEntity.systemMax;
        int start = ((base + source.system - instance.universeEntity.explorationArea) % base) +1;
        for(int i = 0; i < 2*instance.universeEntity.explorationArea+2; i++ ) {
            result.add(new SystemView(source.galaxy, ((start+i)%base)+1));
        }
        return result;
    }
}
