package org.enoch.snark.module.building;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.macro.BuildingEnum;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingManager {

    private final Map<Long, List<BuildingRequest>> levelUpMap;

    public BuildingRequest getBuildRequest(ColonyEntity colony) {
        if(colony.level > levelUpMap.size()) {
            System.err.println(colony+" reach max level " + colony.level);
            return null;
        }
        List<BuildingRequest> levelUpList = levelUpMap.get(colony.level);
        for(BuildingRequest request : levelUpList) {
            Long buildingLevel = colony.getBuildingLevel(request.building);
            if(request.level > buildingLevel) {
//                System.err.println("Maybe build "+request + " on "+colony);
                return request;
            }
        }
        colony.level = colony.level +1;
        ColonyDAO.getInstance().saveOrUpdate(colony);
        return getBuildRequest(colony);
    }

    public Long getColonyLastLevelToProcess() {
        return new Integer(levelUpMap.size()).longValue();
    }

    public BuildingManager() {
        levelUpMap = new HashMap<>();
        levelUpMap.put(1L, Arrays.asList(
                new BuildingRequest(BuildingEnum.solarPlant, 1),
                new BuildingRequest(BuildingEnum.metalMine, 2),
                new BuildingRequest(BuildingEnum.solarPlant, 2),
                new BuildingRequest(BuildingEnum.metalMine, 3),
                new BuildingRequest(BuildingEnum.crystalMine, 1),
                new BuildingRequest(BuildingEnum.solarPlant, 3),
                new BuildingRequest(BuildingEnum.metalMine, 4),
                new BuildingRequest(BuildingEnum.crystalMine, 2),
                new BuildingRequest(BuildingEnum.solarPlant, 4),
                new BuildingRequest(BuildingEnum.crystalMine, 3),
                new BuildingRequest(BuildingEnum.metalMine, 5),
                new BuildingRequest(BuildingEnum.solarPlant, 5),
                new BuildingRequest(BuildingEnum.crystalMine, 4),
                new BuildingRequest(BuildingEnum.metalMine, 6),
                new BuildingRequest(BuildingEnum.solarPlant, 6),
                new BuildingRequest(BuildingEnum.crystalMine, 5),
                new BuildingRequest(BuildingEnum.metalMine, 7),
                new BuildingRequest(BuildingEnum.solarPlant, 7),
                new BuildingRequest(BuildingEnum.crystalMine, 6),
                new BuildingRequest(BuildingEnum.metalMine, 8),
                new BuildingRequest(BuildingEnum.solarPlant, 8),
                new BuildingRequest(BuildingEnum.crystalMine, 7),
                new BuildingRequest(BuildingEnum.metalMine, 9),
                new BuildingRequest(BuildingEnum.solarPlant, 9),
                new BuildingRequest(BuildingEnum.crystalMine, 8),
                new BuildingRequest(BuildingEnum.metalMine, 10),
                new BuildingRequest(BuildingEnum.solarPlant, 10),
                new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 5),
                new BuildingRequest(BuildingEnum.roboticsFactory, 2),
                new BuildingRequest(BuildingEnum.researchLaboratory, 2),
                new BuildingRequest(BuildingEnum.shipyard, 2),
                new BuildingRequest(BuildingEnum.metalMine, 1)));
    }
}
