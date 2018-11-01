package org.enoch.snark.module.explore;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.dao.GalaxyDAO;
import org.enoch.snark.db.dao.impl.GalaxyDAOImpl;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.db.entity.SourceEntity;
import org.enoch.snark.gi.command.impl.GalaxyAnalyzeCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.SystemView;
import org.enoch.snark.module.AbstractModule;

import java.util.*;

public class SpaceModule extends AbstractModule {

    private GalaxyDAO galaxyDAO = new GalaxyDAOImpl(instance.universeEntity);
    Queue<SystemView> toView = new LinkedList<>();

    public SpaceModule(Instance instance) {
        super(instance);
        this.priority = 0;
    }

    private void generateViewArea(List<SystemView> newestGalaxyToView) {
        toView = new LinkedList<>();
        final Collection<SourceEntity> sources = instance.universeEntity.getSourcesById();
        for(SourceEntity source : sources) {
            toView.addAll(generateSystemToView(source));
        }

        System.err.println("domyslnie: " + toView.size());
        System.err.println("znane: " + toView.size());

        deleteExistingsOnes(newestGalaxyToView);

        System.err.println("Systems to view: " + toView.size());
    }

    private void deleteExistingsOnes(List<SystemView> tooNew) {
    int i = 0;
        Queue<SystemView> result = new LinkedList<>();
        for(SystemView view : toView) {
            boolean add = true;
            for (SystemView newView : tooNew) {
                if (view.equals(newView)) {
                    add = false;
                    break;
                }
            }
            if(add) {
                result.add(view);
                i++;
            }
        }
        toView = result;
        System.err.println(i+" stary i do ponownej analizy");

    }

    Collection<SystemView> generateSystemToView(SourceEntity source) {
        List<SystemView> result = new ArrayList<>();
        int base = instance.universeEntity.getSystemMax();
        int start = ((base + source.getSystem() - instance.universeEntity.getExplorationArea()) % base) +1;
        for(int i = 0; i < 2*instance.universeEntity.getExplorationArea()+2; i++ ) {
            result.add(new SystemView(source.getGalaxy(), ((start+i)%base)+1));
        }
        return result;
    }

    @Override
    public void run() {
        final List<GalaxyEntity> latestGalaxyToView = galaxyDAO.findLatestGalaxyToView();

        if(latestGalaxyToView.isEmpty()) {
                generateViewArea(new ArrayList<>());
        } else {
            if(DateUtil.lessThan20H(latestGalaxyToView.get(0).getUpdated())) {
                log.info("No new galaxy to scan");
                return;
            } else {
                generateViewArea(onlyNewest(latestGalaxyToView));
            }
        }

        for (int i = 0; i < 5; i++) {
            final SystemView systemView = toView.poll();
            if(systemView == null) {
                break;
            }
            instance.commander.push(new GalaxyAnalyzeCommand(instance, systemView));
        }
    }

    private List<SystemView> onlyNewest(List<GalaxyEntity> latestGalaxyToView) {
        List<SystemView> result = new ArrayList<>();
        latestGalaxyToView.forEach(galaxyEntity -> {
            if(DateUtil.lessThan20H(galaxyEntity.getUpdated())) {
                result.add(galaxyEntity.toSystemView());
            }
        });
        return result;
    }
}
