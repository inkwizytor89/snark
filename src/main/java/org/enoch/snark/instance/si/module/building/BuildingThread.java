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
import java.util.*;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.model.types.ResourceType.CRYSTAL;
import static org.enoch.snark.instance.model.types.ResourceType.METAL;
import static org.enoch.snark.instance.si.module.ConfigMap.SOURCE;

public class BuildingThread extends AbstractThread {

    public static final String threadType = "building";
    public static final String LIST = "list";
    public static final String SWAP_TRANSPORT = "swap_transport";

    public static final String DEFAULT_LIST = "small";
    public static final int SHORT_PAUSE = 20;

    private Map<ColonyEntity, Queue<BuildingRequest>> colonyMap;
    private final QueueManger queueManger;
    private final BuildingCost buildingCost;
    private String buildingList;

    public BuildingThread(ConfigMap map) {
        super(map);
        buildingCost = BuildingCost.getInstance();
        queueManger = QueueManger.getInstance();
    }

    @Override
    protected String getThreadType() {
        return threadType;
    }

    @Override
    protected int getPauseInSeconds() {
        return SHORT_PAUSE; //maybe should wait number of colonies *10 or min level of colonies
    }

    @Override
    protected void onStep() {
        updateSourceMap();
        for(ColonyEntity colony : colonyMap.keySet()) {
            Queue<BuildingRequest> requests = colonyMap.get(colony);
            if(requests == null) {
                requests = BuildingManager.getBuildRequests(buildingList, debug);
                colonyMap.put(colony, requests);
            }
            while(skipAlreadyBuild(colony, requests.peek())) requests.poll();
            if (requests.isEmpty()) continue; //nothing to build

            BuildingRequest buildingRequest = requests.peek();
            if(isColonyNotYetLoaded(colony) ||
                    isQueueBusy(colony, buildingRequest) ||
                    isUpgradeBlocked(colony, buildingRequest))
                continue;

            Resources resources = buildingCost.getCosts(buildingRequest);
            BuildRequirements buildRequirements = new BuildRequirements(buildingRequest, resources);
            if(buildRequirements.isResourceUnknown() || buildRequirements.canBuildOn(colony)) {
               new BuildCommand(colony, buildRequirements).push();
            } else {
                ColonyEntity swapColony = ColonyDAO.getInstance().find(colony.cpm);
                Boolean swapTransport = map.getConfigBoolean(SWAP_TRANSPORT, true);
                if (swapTransport && buildRequirements.canBuildOn(swapColony)) {
                    Resources requieredResources = buildRequirements.resources.skipLeave(METAL, CRYSTAL);
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

    private boolean isUpgradeBlocked(ColonyEntity colony, BuildingRequest buildingRequest) {
//        buildingRequest.building.isLifeform()
        return false;
    }

    private boolean skipAlreadyBuild(ColonyEntity colony, BuildingRequest request) {
        if(request == null) return false;
        Long buildingLevel = colony.getBuildingLevel(request.building);
        return request.level <= buildingLevel;
    }

    private void updateSourceMap() {
        String configList = map.getConfig(LIST, DEFAULT_LIST);
        if(!configList.equals(buildingList)) {
            colonyMap = new HashMap<>();
            buildingList = configList;
        }
        List<ColonyEntity> planets = new ArrayList<>(map.getColonies(SOURCE, PlanetExpression.PLANET));
        Util.updateMapKeys(colonyMap, planets, null);
    }

    private boolean isColonyNotYetLoaded(ColonyEntity colony) {
        return colony.level == null;
    }

    private boolean isQueueBusy(ColonyEntity colony, BuildingRequest buildingRequest) {
        String queue = buildingRequest.building.isLifeform() ? QueueManger.LIFEFORM_BUILDINGS : QueueManger.BUILDING;
        return !queueManger.isFree(colony, queue);
    }
}
