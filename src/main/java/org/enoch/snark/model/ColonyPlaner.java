package org.enoch.snark.model;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.Instance;

import java.util.*;

public class ColonyPlaner {

    private final HashMap<ColonyEntity, Long> distanceMap;
    private ColonyEntity nearestColony;

    public ColonyPlaner(TargetEntity target) {
        this(target.toPlanet());
    }

    public ColonyPlaner(Planet planet) {
        this(planet, new ArrayList<>(Instance.getInstance().flyPoints));
    }

    public ColonyPlaner(Planet planet, List<ColonyEntity> range) {
        distanceMap = new HashMap<>();
        range.forEach(colony -> distanceMap.put(colony, planet.calculateDistance(colony.toPlanet())));

        nearestColony = distanceMap.entrySet().stream()
                .filter(colony -> !colony.getKey().toPlanet().equals(planet))
                .min(Comparator.comparingLong(Map.Entry::getValue)).get().getKey();
    }

    public ColonyEntity getNearestColony() {
        return nearestColony;
    }

}
