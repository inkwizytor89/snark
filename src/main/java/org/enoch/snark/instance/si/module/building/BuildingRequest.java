package org.enoch.snark.instance.si.module.building;

import org.enoch.snark.gi.types.BuildingEnum;

public class BuildingRequest {

    public BuildingEnum building;
    public Long level;

    public BuildingRequest(BuildingEnum building, long level) {
        this.building = building;
        this.level = level;
    }

    @Override
    public String toString() {
        return building.name() + " " + level;
    }
}
