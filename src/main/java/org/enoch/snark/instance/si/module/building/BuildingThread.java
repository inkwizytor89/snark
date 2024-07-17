package org.enoch.snark.instance.si.module.building;

import org.enoch.snark.common.Util;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.command.impl.BuildCommand;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.model.action.FleetBuilder;
import org.enoch.snark.instance.model.action.PlanetExpression;
import org.enoch.snark.instance.model.action.QueueManger;
import org.enoch.snark.instance.model.action.condition.ResourceCondition;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ConfigMap;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.model.types.ResourceType.CRYSTAL;
import static org.enoch.snark.instance.model.types.ResourceType.METAL;
import static org.enoch.snark.instance.si.module.ConfigMap.NAME;
import static org.enoch.snark.instance.si.module.ConfigMap.SOURCE;

public class BuildingThread extends AbstractThread {

    public static final String threadName = "building";
    public static final int SHORT_PAUSE = 20;
    public static final String LEVEL = "level";
    public static final String SWAP_TRANSPORT = "swap_transport";
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
        Integer lastLevel = getColonyLastLevelToProcess();
        for(ColonyEntity colony : colonyMap.keySet()) {
            if(colony.level > lastLevel) continue;
            if(isColonyNotYetLoaded(colony) || isQueueBusy(colony)) continue;

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
                Boolean swapTransport = map.getConfigBoolean(SWAP_TRANSPORT, true);
                if (swapTransport && requirements.canBuildOn(swapColony)) {
                    Resources requieredResources = requirements.resources.skipLeave(METAL, CRYSTAL);
                    new FleetBuilder()
                            .from(swapColony)
                            .mission(Mission.TRANSPORT)
                            .addCondition(new ResourceCondition(requieredResources))
                            .resources(requieredResources)
                            .buildOne()
                            .push(LocalDateTime.now().minusMinutes(10L));
                }
            }
        }
    }

    private void updateSourceMap() {
        List<ColonyEntity> planets = map.getColonies(SOURCE, PlanetExpression.PLANET).stream()
                .filter(colonyEntity -> ColonyType.PLANET.equals(colonyEntity.type))
                .collect(Collectors.toList());
        Util.updateMapKeys(colonyMap, planets, null);
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
