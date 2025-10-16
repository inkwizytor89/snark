package org.enoch.snark.instance.si.module.building;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.FleetSendCommand;
import org.enoch.snark.action.command.SendCommand;
import org.enoch.snark.common.Util;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.action.command.BuildCommand;
import org.enoch.snark.instance.model.action.PlanetExpression;
import org.enoch.snark.instance.model.uc.ResourceUC;
import org.enoch.snark.instance.service.TechnologyService;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.util.*;

import static org.enoch.snark.action.command.FollowingAction.DELAY_TO_FLEET_BACK;
import static org.enoch.snark.action.command.FollowingAction.DELAY_TO_FLEET_THERE;
import static org.enoch.snark.instance.model.to.Resources.nothing;
import static org.enoch.snark.instance.model.uc.FleetUC.transportFleet;
import static org.enoch.snark.instance.si.module.ThreadMap.SOURCE;

@RequiredArgsConstructor
public class BuildThread extends AbstractThread {

    public static final String threadType = "build";
    public static final String LIST = "list";
    public static final String SWAP_TRANSPORT = "swap_transport";

    public static final String DEFAULT_LIST = "small";
    public static final int SHORT_PAUSE = 20;

    private Map<ColonyEntity, Queue<BuildRequest>> colonyMap;
    private TechnologyService technologyService  = TechnologyService.getInstance();
    private BuildingCost buildingCost = BuildingCost.getInstance();
    private String buildingList;

    @Override
    protected String getThreadType() {
        return threadType;
    }

    @Override
    protected String defaultPause() {
        return SHORT_PAUSE+"S";
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

            SendCommand pushedCommand = null;
            if (map.getConfigBoolean(SWAP_TRANSPORT, true))
                pushedCommand = transportNearResourcesAndBuild(colony, requirements);
            if(pushedCommand != null) log("Move "+pushedCommand.hash()+" missing resource and push build on "+colony+" "+colony.getResources()+" where requirements:"+requirements);
            else log("On "+colony+" "+colony.getResources()+" is not enough to start requirements:"+requirements);
        }
    }

    private boolean isBuildQueueBlockedForBuildRequest(ColonyEntity colony, BuildRequest buildRequest) {
        return technologyService.isBlocked(colony, buildRequest.technology);
    }

    private SendCommand transportNearResourcesAndBuild(ColonyEntity colony, BuildRequirements requirements) {
        Optional<ColonyEntity> swapColony = colonyRepository.findByCp(colony.cpm);
        if(swapColony.isEmpty()) return null;
        Resources leaveColony = map.getNearestLeaveResources(colony.type, nothing);
        Resources missing = requirements.resources.missing(colony.getResources().missing(leaveColony));
        Resources leaveSwap = map.getNearestLeaveResources(swapColony.get().type, nothing);
        FleetSendCommand command = transportFleet(swapColony.get(), colony, missing, leaveSwap);
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
