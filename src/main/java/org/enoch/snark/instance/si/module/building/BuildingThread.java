package org.enoch.snark.instance.si.module.building;

import org.enoch.snark.common.Util;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.command.impl.BuildCommand;
import org.enoch.snark.gi.command.impl.SendFleetPromiseCommand;
import org.enoch.snark.instance.model.action.PlanetExpression;
import org.enoch.snark.instance.model.uc.ResourceUC;
import org.enoch.snark.instance.service.TechnologyService;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ThreadMap;

import java.util.*;

import static org.enoch.snark.gi.command.impl.FollowingAction.DELAY_TO_FLEET_BACK;
import static org.enoch.snark.gi.command.impl.FollowingAction.DELAY_TO_FLEET_THERE;
import static org.enoch.snark.instance.model.to.Resources.nothing;
import static org.enoch.snark.instance.model.uc.FleetUC.transportFleet;
import static org.enoch.snark.instance.si.module.ThreadMap.SOURCE;

public class BuildingThread extends AbstractThread {

    public static final String threadType = "building";
    public static final String LIST = "list";
    public static final String SWAP_TRANSPORT = "swap_transport";

    public static final String DEFAULT_LIST = "small";
    public static final int SHORT_PAUSE = 20;

    private Map<ColonyEntity, Queue<BuildRequest>> colonyMap;
    private final TechnologyService technologyService;
    private final BuildingCost buildingCost;
    private String buildingList;

    public BuildingThread(ThreadMap map) {
        super(map);
        buildingCost = BuildingCost.getInstance();
        technologyService = TechnologyService.getInstance();
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

            BuildRequest buildRequest = getNextRequest(colony);
            if (isNothingToBuild(buildRequest)) continue;
            if (isBuildQueueBlockedForBuildRequest(colony, buildRequest)) continue;

            BuildRequirements requirements = new BuildRequirements(buildRequest, buildingCost.getCosts(buildRequest));
            Resources leave = map.getNearestLeaveResources(colony.type, nothing);
            if(requirements.isResourceUnknown() || ResourceUC.toTransport(colony, requirements.resources, leave) != null) { // moze colony.hasEnoughResources powinno miec to zaszyte
                log("Push build on "+colony+" "+colony.getResources()+" where requirements:"+requirements);
                new BuildCommand(colony, requirements).push();
               continue;
            }

            SendFleetPromiseCommand pushedCommand = null;
            if (map.getConfigBoolean(SWAP_TRANSPORT, true))
                pushedCommand = transportNearResourcesAndBuild(colony, requirements);
            if(pushedCommand != null) log("Move "+pushedCommand.hash()+" missing resource and push build on "+colony+" "+colony.getResources()+" where requirements:"+requirements);
            else log("On "+colony+" "+colony.getResources()+" is not enough to start requirements:"+requirements);
        }
    }

    private boolean isBuildQueueBlockedForBuildRequest(ColonyEntity colony, BuildRequest buildRequest) {
        return technologyService.isBlocked(colony, buildRequest.technology);
    }

    private SendFleetPromiseCommand transportNearResourcesAndBuild(ColonyEntity colony, BuildRequirements requirements) {
        ColonyEntity swapColony = ColonyDAO.getInstance().find(colony.cpm);
        if(swapColony == null ) return null;
        Resources leaveColony = map.getNearestLeaveResources(colony.type, nothing);
        Resources missing = requirements.resources.missing(colony.getResources().missing(leaveColony));
        Resources leaveSwap = map.getNearestLeaveResources(swapColony.type, nothing);
        SendFleetPromiseCommand command = transportFleet(swapColony, colony, missing, leaveSwap);
        if(command != null) {
            command.setNext(new BuildCommand(colony, requirements),DELAY_TO_FLEET_THERE);
            command.push(DELAY_TO_FLEET_BACK);
        }
        return command;
    }

    private static boolean isNothingToBuild(BuildRequest buildRequest) {
        return buildRequest == null;
    }

    private BuildRequest getNextRequest(ColonyEntity colony) {
        Queue<BuildRequest> requests = colonyMap.get(colony);
        if(requests == null) {
            requests = BuildingManager.getBuildRequests(buildingList, debug);
            colonyMap.put(colony, requests);
        }
        while(skipAlreadyBuild(colony, requests.peek())) requests.poll();
        return requests.peek();
    }

    private boolean skipAlreadyBuild(ColonyEntity colony, BuildRequest request) {
        if(isNothingToBuild(request)) return false;
        Long buildingLevel = colony.getBuildingLevel(request.technology);
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
}
