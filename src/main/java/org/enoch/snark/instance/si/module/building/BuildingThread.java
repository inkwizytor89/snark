package org.enoch.snark.instance.si.module.building;

import org.enoch.snark.common.Util;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.command.impl.BuildCommand;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.model.action.FleetBuilder;
import org.enoch.snark.instance.model.action.QueueManger;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ConfigMap;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.si.module.ConfigMap.NAME;
import static org.enoch.snark.instance.si.module.ConfigMap.SOURCE;

public class BuildingThread extends AbstractThread {

    public static final String threadName = "building";
    public static final int SHORT_PAUSE = 20;
    public static final String LEVEL = "level";
    private Map<ColonyEntity, BuildRequirements> colonyMap = new HashMap<>();
    private int pause = SHORT_PAUSE;
    private final QueueManger queueManger;
    private BuildingManager buildingManager;
    private ColonyDAO colonyDAO;
    private BuildingCost buildingCost;

    public BuildingThread(ConfigMap map) {
        super(map);
        buildingManager = new BuildingManager(map.getConfig(NAME));
        colonyDAO = ColonyDAO.getInstance();
        buildingCost = BuildingCost.getInstance();
        queueManger = QueueManger.getInstance();
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return pause; //maybe should wait number of colonies *10 or min level of colonies
    }

    @Override
    protected void onStep() {
        updateSourceMap();
//        System.err.println(queueManger);
        for(ColonyEntity colony : colonyMap.keySet()) {
            if(colony.level > getColonyLastLevelToProcess()) {
                continue;
            }
            if(isColonyNotYetLoaded(colony) || isQueueBusy(colony)) {
//                System.err.println("colony "+colony+" not ready "+ isColonyNotYetLoaded(colony) +" "+ isQueueBusy(colony));
                continue;
            }

            BuildRequirements requirements = colonyMap.get(colony);
            if(requirements == null || requirements.isResourceUnknown()) {
                BuildingRequest buildRequest = buildingManager.getBuildRequest(colony);
                if (buildRequest == null) continue; //nothing to build
                Resources resources = buildingCost.getCosts(buildRequest);
                requirements = new BuildRequirements(buildRequest, resources);
                colonyMap.put(colony, requirements);
            }
            if(requirements.canBuildOn(colony)) {
                new BuildCommand(colony, requirements).push();
                colonyMap.put(colony, null);
            } else {
                ColonyEntity swapColony = ColonyDAO.getInstance().find(colony.cpm);
                if (requirements.canBuildOn(swapColony)) {
                    new FleetBuilder()
                            .from(swapColony)
                            .mission(Mission.TRANSPORT)
                            .resources(requirements.resources)
                            .buildOne()
                            .push(LocalDateTime.now().minusMinutes(10L));
                }
            }
        }
//        System.err.println("Building end step, sleep in "+pause);
    }

    private void updateSourceMap() {
        List<ColonyEntity> planets = map.getColonies(SOURCE, "planet").stream()
                .filter(colonyEntity -> ColonyType.PLANET.equals(colonyEntity.type))
                .collect(Collectors.toList());
        Util.updateMapKeys(colonyMap, planets, null);
//        colonyDAO.fetchAll().stream()
//                .filter(colony -> colony.isPlanet)
//                .forEach(colony -> colonyMap.put(colony, null));
    }

    public Integer getColonyLastLevelToProcess() {
        return map.getConfigInteger(LEVEL, 2);
    }

    private boolean isColonyNotYetLoaded(ColonyEntity colony) {
        return colony.level == null;
    }

    private boolean isQueueBusy(ColonyEntity colony) {
        return !queueManger.isFree(colony, QueueManger.BUILDING);
    }
}
