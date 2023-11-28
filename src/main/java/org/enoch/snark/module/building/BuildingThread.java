package org.enoch.snark.module.building;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.command.impl.BuildCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.QueueManger;
import org.enoch.snark.model.Resources;
import org.enoch.snark.module.AbstractThread;
import org.enoch.snark.module.ConfigMap;

import java.util.HashMap;
import java.util.Map;

public class BuildingThread extends AbstractThread {

    public static final String threadName = "building";
    public static final int SHORT_PAUSE = 20;
    public static final String LEVEL = "level";
    private final Map<ColonyEntity, BuildRequirements> colonyMap = new HashMap<>();
    private int pause = SHORT_PAUSE;
    private final QueueManger queueManger;
    private BuildingManager buildingManager;
    private ColonyDAO colonyDAO;
    private BuildingCost buildingCost;

    public BuildingThread(ConfigMap map) {
        super(map);
        buildingManager = new BuildingManager();
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
    protected void onStart() {
        super.onStart();
        colonyDAO.fetchAll().stream()
                .filter(colony -> colony.isPlanet)
                .forEach(colony -> colonyMap.put(colony, null));
    }

    @Override
    protected void onStep() {
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
                Instance.getInstance().commander.push(new BuildCommand(colony, requirements));
                colonyMap.put(colony, null);
            }
        }
//        System.err.println("Building end step, sleep in "+pause);
    }
    public static Integer getColonyLastLevelToProcess() {
        return Instance.config.getConfigInteger(threadName, LEVEL, 2);
    }

    private boolean isColonyNotYetLoaded(ColonyEntity colony) {
        return colony.level == null;
    }

    private boolean isQueueBusy(ColonyEntity colony) {
        return !queueManger.isFree(colony, QueueManger.BUILDING);
    }
}
