package org.enoch.snark.module.building;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.command.impl.BuildCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.SI;
import org.enoch.snark.instance.ommander.QueueManger;
import org.enoch.snark.model.Resources;
import org.enoch.snark.module.AbstractThread;

import java.util.HashMap;
import java.util.Map;

public class BuildingThread extends AbstractThread {

    public static final String threadName = "building";
    public static final int SHORT_PAUSE = 40;
    public static final int LONG_PAUSE = 600;

    private final Map<ColonyEntity, BuildRequirements> colonyMap = new HashMap<>();
    private int pause = SHORT_PAUSE;
    private final QueueManger queueManger;
    private BuildingManager buildingManager;
    private ColonyDAO colonyDAO;
    private BuildingCost buildingCost;

    public BuildingThread(SI si) {
        super(si);
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
        return pause;
    }

    @Override
    protected void onStart() {
        super.onStart();
        colonyDAO.fetchAll().forEach(colony -> colonyMap.put(colony, null));
    }

    @Override
    protected void onStep() {
        System.err.println(queueManger);
        System.err.println("Building start step");
        pause = LONG_PAUSE;
        for(ColonyEntity colony : colonyMap.keySet()) {

            if(!queueManger.isFree(colony, QueueManger.BUILDING)) {
                continue;
            }

            BuildRequirements requirements = colonyMap.get(colony.refresh());
            if(requirements == null || requirements.isResourceUnknown()) {
                BuildingRequest buildRequest = buildingManager.getBuildRequest(colony);
                if (buildRequest == null) continue; //nothing to build
                Resources resources = buildingCost.getCosts(buildRequest);
                requirements = new BuildRequirements(buildRequest, resources);
                colonyMap.put(colony, requirements);
            }
            if(requirements.canBuildOn(colony)) {
                pause = SHORT_PAUSE;
                Instance.getInstance().commander.push(new BuildCommand(colony, requirements));
                colonyMap.put(colony, null);
            }
        }
        System.err.println("Building end step");
    }
}
