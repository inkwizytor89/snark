package org.enoch.snark.module.building;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.macro.BuildingEnum;

import java.util.Arrays;
import java.util.List;

public class BuildingManager {
    public BuildingRequest getBuildRequest(ColonyEntity colony) {
        for(BuildingRequest request : listToBuild) {
            Long buildingLevel = colony.getBuildingLevel(request.building);
            if(request.level > buildingLevel) {
                System.err.println("Maybe build "+request + " on "+colony);
                return request;
            }
        }
        return null;
    }

    private List<BuildingRequest> listToBuild = Arrays.asList(
            new BuildingRequest(BuildingEnum.metalStorage, 11),
            new BuildingRequest(BuildingEnum.metalStorage, 12),
            new BuildingRequest(BuildingEnum.metalStorage, 13),
            new BuildingRequest(BuildingEnum.metalStorage, 14),
            new BuildingRequest(BuildingEnum.metalStorage, 15),
            new BuildingRequest(BuildingEnum.metalStorage, 16),
            new BuildingRequest(BuildingEnum.metalStorage, 17),
            new BuildingRequest(BuildingEnum.metalStorage, 18),
            new BuildingRequest(BuildingEnum.metalStorage, 19),
            new BuildingRequest(BuildingEnum.metalStorage, 111));
}
