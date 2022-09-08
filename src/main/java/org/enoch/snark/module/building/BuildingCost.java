package org.enoch.snark.module.building;

import org.enoch.snark.gi.macro.BuildingEnum;
import org.enoch.snark.model.Resources;

import java.util.HashMap;
import java.util.Map;

public class BuildingCost {

    private static BuildingCost INSTANCE;
    Map<BuildingEnum,Map<Long, Resources>> costs;

    private BuildingCost() {
        costs = new HashMap<>();

        for(BuildingEnum building : BuildingEnum.values()) {
            costs.put(building, new HashMap<>());
        }
    }

    public static BuildingCost getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new BuildingCost();
        }
        return INSTANCE;
    }

    public Resources getCosts(BuildingRequest request) {
        Map<Long, Resources> costPerLevel = costs.get(request.building);
        if(!costPerLevel.containsKey(request.level)) {
            return Resources.unknown;
        } else {
            return costPerLevel.get(request.level);
        }
    }

    public void put(BuildingRequest request, Resources resources) {
        costs.get(request.building).put(request.level, resources);
    }
}
