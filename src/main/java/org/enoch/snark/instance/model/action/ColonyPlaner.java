package org.enoch.snark.instance.model.action;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.to.Planet;

import java.util.*;

public class ColonyPlaner {

    private final List<ColonyEntity> colonies;

    public ColonyPlaner() {
        this(Instance.getInstance().flyPoints);
    }

    public ColonyPlaner(List<ColonyEntity> colonies) {
        this.colonies = colonies;
    }

    public ColonyEntity getNearestColony(TargetEntity target) {
        return getNearestColony(target.toPlanet());
    }

    public ColonyEntity getNearestColony(Planet planet) {
        HashMap<ColonyEntity, Long> distanceMap = new HashMap<>();
        colonies.forEach(colony -> distanceMap.put(colony, planet.calculateDistance(colony.toPlanet())));
        return distanceMap.entrySet().stream()
                .filter(colony -> !colony.getKey().toPlanet().equals(planet))
                .min(Comparator.comparingLong(Map.Entry::getValue)).get().getKey();
    }

    public ColonyEntity findSimilar(Planet planet) {
        HashMap<ColonyEntity, Long> distanceMap = new HashMap<>();
        colonies.forEach(colony -> distanceMap.put(colony, planet.calculateDistance(colony.toPlanet())));
        return distanceMap.entrySet().stream()
                .min(Comparator.comparingLong(Map.Entry::getValue)).get().getKey();
    }

    public boolean isNear(TargetEntity target) {
        return isNear(target.toPlanet());
    }

    public boolean isNear(Planet planet) {
        return isNear(planet, 13000L);
    }

    public boolean isNear(TargetEntity target, Long maxDistance) {
        return isNear(target.toPlanet(), maxDistance);
    }

    public boolean isNear(Planet planet, Long maxDistance) {
        return colonies.stream().anyMatch(colony -> planet.calculateDistance(colony.toPlanet()) < maxDistance);
    }

    public static Long mapSystemToDistance(Integer systemMax) {
        if(systemMax == -1) return 13000L;
        Long systemMaxLong = Long.valueOf(systemMax);
        return systemMaxLong* 95 + 2700;
    }

}
