package org.enoch.snark.instance.si.module.building;

import org.enoch.snark.instance.model.technology.Technology;
import org.enoch.snark.instance.model.to.Resources;

import java.util.HashMap;
import java.util.Map;

public class BuildingCost {

    private static BuildingCost INSTANCE;
    Map<Technology,Map<Long, Resources>> costs;

    private BuildingCost() {
        costs = new HashMap<>();
    }

    public static BuildingCost getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new BuildingCost();
        }
        return INSTANCE;
    }

    public Resources getCosts(BuildRequest request) {
        Map<Long, Resources> costPerLevel = costs.get(request.technology);
        if(!costs.containsKey(request.technology) || !costPerLevel.containsKey(request.level)) {
            return Resources.unknown;
        } else {
            return costPerLevel.get(request.level);
        }
    }

    public void put(BuildRequest request, Resources resources) {
        if(!costs.containsKey(request.technology)) costs.put(request.technology, new HashMap<>());

        Map<Long, Resources> technologyCosts = costs.get(request.technology);
        if(!technologyCosts.containsKey(request.level))
            technologyCosts.put(request.level, resources);
    }
}
