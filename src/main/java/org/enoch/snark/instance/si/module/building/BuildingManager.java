package org.enoch.snark.instance.si.module.building;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.gi.types.BuildingEnum;
import org.enoch.snark.instance.Instance;

import java.util.*;

import static org.enoch.snark.instance.si.module.building.BuildingThread.LEVEL;

public class BuildingManager {
    private String tag;

//    private final Map<Long, List<BuildingRequest>> levelUpMap;

    public BuildingRequest getBuildRequest(ColonyEntity colony) {
        final Map<Long, List<BuildingRequest>> levelUpMap = generateLevelUpMap(colony);
        if(colony.level > levelUpMap.size()) {
            System.err.println(colony+" reach max level " + colony.level);
            return null;
        }
        if (!levelUpMap.containsKey(colony.level)) {
            return null;
        }
        for(BuildingRequest request : levelUpMap.get(colony.level)) {
            Long buildingLevel = colony.getBuildingLevel(request.building);
            if(request.level > buildingLevel) {
//                System.err.println("Maybe build "+request + " on "+colony);
                return request;
            }
        }
        colony.level = colony.level +1;
        return getBuildRequest(colony);
    }

    private Map<Long, List<BuildingRequest>> generateLevelUpMap(ColonyEntity colony) {
        final Map<Long, List<BuildingRequest>> map = new HashMap<>();
        final int colonyCount = ColonyDAO.getInstance().fetchAll().size();
        final Integer maxLevel = getColonyLastLevelToProcess();
        if (colonyCount < 5) addSimpleBuildings(map, maxLevel);
        else addFastGrowBuildings(map, maxLevel);
        addMines(map, colony, maxLevel);
        return map;
    }

    public Integer getColonyLastLevelToProcess() {
        return Instance.getConfigMap(tag).getConfigInteger(LEVEL, 2);
    }

    private void addMines(Map<Long, List<BuildingRequest>> map, ColonyEntity colony, Integer maxLevel) {
        if(colony.position >= 13) {
            for(long i=3; i<=maxLevel; i++) {
                ArrayList<BuildingRequest> list = new ArrayList<>();
                list.add(new BuildingRequest(BuildingEnum.deuteriumSynthesizer, i));
                list.add(new BuildingRequest(BuildingEnum.crystalMine, i-1));
                list.add(new BuildingRequest(BuildingEnum.metalMine, i-2));
                list.add(new BuildingRequest(BuildingEnum.fusionPlant, i/2));
                map.put(i, list);
            }
        } else {
            for(long i=3; i<=maxLevel; i++) {
                ArrayList<BuildingRequest> list = new ArrayList<>();
                list.add(new BuildingRequest(BuildingEnum.deuteriumSynthesizer, i-1));
                list.add(new BuildingRequest(BuildingEnum.crystalMine, i-2));
                list.add(new BuildingRequest(BuildingEnum.metalMine, i));
                map.put(i, list);
            }
        }
    }

    private void addFastGrowBuildings(Map<Long, List<BuildingRequest>> map, Integer maxLevel) {
        if(maxLevel >= 1L)
          map.put(1L, Arrays.asList(
                new BuildingRequest(BuildingEnum.roboticsFactory, 10),
                new BuildingRequest(BuildingEnum.naniteFactory, 1)
//                new BuildingRequest(BuildingEnum.deuteriumStorage, 7),
//                new BuildingRequest(BuildingEnum.crystalStorage, 8),
//                new BuildingRequest(BuildingEnum.metalStorage, 9),
//                new BuildingRequest(BuildingEnum.shipyard, 8)
          ));

        if(maxLevel >= 2L)
            map.put(2L, Arrays.asList(
            new BuildingRequest(BuildingEnum.solarPlant, 1),
            new BuildingRequest(BuildingEnum.metalMine, 2),
            new BuildingRequest(BuildingEnum.solarPlant, 2),
            new BuildingRequest(BuildingEnum.metalMine, 3),
            new BuildingRequest(BuildingEnum.crystalMine, 1),
            new BuildingRequest(BuildingEnum.solarPlant, 3),
            new BuildingRequest(BuildingEnum.metalMine, 4),
            new BuildingRequest(BuildingEnum.crystalMine, 2),
            new BuildingRequest(BuildingEnum.solarPlant, 4),
            new BuildingRequest(BuildingEnum.metalMine, 5),
            new BuildingRequest(BuildingEnum.crystalMine, 3),
            new BuildingRequest(BuildingEnum.solarPlant, 5),
            new BuildingRequest(BuildingEnum.metalMine, 6),
            new BuildingRequest(BuildingEnum.crystalMine, 4),
            new BuildingRequest(BuildingEnum.solarPlant, 6),
            new BuildingRequest(BuildingEnum.metalMine, 7),
            new BuildingRequest(BuildingEnum.crystalMine, 5),
            new BuildingRequest(BuildingEnum.solarPlant, 7),
            new BuildingRequest(BuildingEnum.metalMine, 8),
            new BuildingRequest(BuildingEnum.crystalMine, 6),
            new BuildingRequest(BuildingEnum.solarPlant, 8),
            new BuildingRequest(BuildingEnum.metalMine, 9),
            new BuildingRequest(BuildingEnum.crystalMine, 7),
            new BuildingRequest(BuildingEnum.solarPlant, 9),
            new BuildingRequest(BuildingEnum.metalMine, 10),
            new BuildingRequest(BuildingEnum.crystalMine, 8),
            new BuildingRequest(BuildingEnum.solarPlant, 10),
            new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 5),
            new BuildingRequest(BuildingEnum.solarPlant, 11),
            new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 7)
        ));
    }

    private void addSimpleBuildings(Map<Long, List<BuildingRequest>> map, Integer maxLevel) {
        if(maxLevel >= 1L)
            map.put(1L, Arrays.asList(
                    new BuildingRequest(BuildingEnum.solarPlant, 1),
                    new BuildingRequest(BuildingEnum.metalMine, 2),
                    new BuildingRequest(BuildingEnum.solarPlant, 2),
                    new BuildingRequest(BuildingEnum.metalMine, 3),
                    new BuildingRequest(BuildingEnum.crystalMine, 1),
                    new BuildingRequest(BuildingEnum.solarPlant, 3),
                    new BuildingRequest(BuildingEnum.metalMine, 4),
                    new BuildingRequest(BuildingEnum.crystalMine, 2),
                    new BuildingRequest(BuildingEnum.solarPlant, 4),
                    new BuildingRequest(BuildingEnum.metalMine, 5),
                    new BuildingRequest(BuildingEnum.crystalMine, 3),
                    new BuildingRequest(BuildingEnum.solarPlant, 5),
                    new BuildingRequest(BuildingEnum.metalMine, 6),
                    new BuildingRequest(BuildingEnum.crystalMine, 4),
                    new BuildingRequest(BuildingEnum.solarPlant, 6),
                    new BuildingRequest(BuildingEnum.metalMine, 7),
                    new BuildingRequest(BuildingEnum.crystalMine, 5),
                    new BuildingRequest(BuildingEnum.solarPlant, 7),
                    new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 3),
                    new BuildingRequest(BuildingEnum.solarPlant, 8),
                    new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 5),
                    new BuildingRequest(BuildingEnum.solarPlant, 9),


                    new BuildingRequest(BuildingEnum.metalStorage, 1),
                    new BuildingRequest(BuildingEnum.crystalStorage, 1),
                    new BuildingRequest(BuildingEnum.deuteriumStorage, 1),

                    new BuildingRequest(BuildingEnum.roboticsFactory, 2),
                    new BuildingRequest(BuildingEnum.researchLaboratory, 1),
                    new BuildingRequest(BuildingEnum.shipyard, 2),

                    new BuildingRequest(BuildingEnum.metalMine, 8),
                    new BuildingRequest(BuildingEnum.crystalMine, 6),
                    new BuildingRequest(BuildingEnum.metalMine, 9),
                    new BuildingRequest(BuildingEnum.solarPlant, 10),
                    new BuildingRequest(BuildingEnum.crystalMine, 7),
                    new BuildingRequest(BuildingEnum.metalMine, 10),
                    new BuildingRequest(BuildingEnum.crystalMine, 8),
                    new BuildingRequest(BuildingEnum.solarPlant, 11),

                    new BuildingRequest(BuildingEnum.metalMine, 11),
                new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 7)
            ));

        if(maxLevel >= 2L)
            map.put(2L, Arrays.asList(
                new BuildingRequest(BuildingEnum.solarPlant, 12),
                    new BuildingRequest(BuildingEnum.metalMine, 12),
                new BuildingRequest(BuildingEnum.crystalMine, 10),
                new BuildingRequest(BuildingEnum.solarPlant, 13),

                    new BuildingRequest(BuildingEnum.metalMine, 13),
                    new BuildingRequest(BuildingEnum.crystalMine, 11),
                    new BuildingRequest(BuildingEnum.solarPlant, 14),

                    new BuildingRequest(BuildingEnum.metalMine, 14),
                    new BuildingRequest(BuildingEnum.crystalMine, 12),
                    new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 10),

                    new BuildingRequest(BuildingEnum.metalStorage, 2),
                    new BuildingRequest(BuildingEnum.solarPlant, 15),

                    new BuildingRequest(BuildingEnum.metalStorage, 3),
                    new BuildingRequest(BuildingEnum.deuteriumStorage, 2),
                    new BuildingRequest(BuildingEnum.crystalStorage, 2),
                    new BuildingRequest(BuildingEnum.solarPlant, 16),

                    new BuildingRequest(BuildingEnum.metalStorage, 4),
                    new BuildingRequest(BuildingEnum.crystalStorage, 3),
                new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 12),
                new BuildingRequest(BuildingEnum.crystalMine, 13),
                new BuildingRequest(BuildingEnum.roboticsFactory, 4),
                new BuildingRequest(BuildingEnum.shipyard, 4)
                ));
    }

    public BuildingManager(String tag) {
        this.tag = tag;
//        levelUpMap = new HashMap<>();
//        levelUpMap.put(1L, Arrays.asList(
//                new BuildingRequest(BuildingEnum.solarPlant, 1),
//                new BuildingRequest(BuildingEnum.metalMine, 2),
//                new BuildingRequest(BuildingEnum.solarPlant, 2),
//                new BuildingRequest(BuildingEnum.metalMine, 3),
//                new BuildingRequest(BuildingEnum.crystalMine, 1),
//                new BuildingRequest(BuildingEnum.solarPlant, 3),
//                new BuildingRequest(BuildingEnum.metalMine, 4),
//                new BuildingRequest(BuildingEnum.crystalMine, 2),
//                new BuildingRequest(BuildingEnum.solarPlant, 4),
//                new BuildingRequest(BuildingEnum.metalMine, 5),
//                new BuildingRequest(BuildingEnum.crystalMine, 3),
//                new BuildingRequest(BuildingEnum.solarPlant, 5),
//                new BuildingRequest(BuildingEnum.metalMine, 6),
//                new BuildingRequest(BuildingEnum.crystalMine, 4),
//                new BuildingRequest(BuildingEnum.solarPlant, 6),
//                new BuildingRequest(BuildingEnum.metalMine, 7),
//                new BuildingRequest(BuildingEnum.crystalMine, 5),
//                new BuildingRequest(BuildingEnum.solarPlant, 7),
//                new BuildingRequest(BuildingEnum.metalMine, 8),
//                new BuildingRequest(BuildingEnum.crystalMine, 6),
//                new BuildingRequest(BuildingEnum.solarPlant, 8),
//                new BuildingRequest(BuildingEnum.metalMine, 9),
//                new BuildingRequest(BuildingEnum.crystalMine, 7),
//                new BuildingRequest(BuildingEnum.solarPlant, 9),
//                new BuildingRequest(BuildingEnum.metalMine, 10),
//                new BuildingRequest(BuildingEnum.crystalMine, 8),
//                new BuildingRequest(BuildingEnum.solarPlant, 10),
//                new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 5),
//                new BuildingRequest(BuildingEnum.solarPlant, 11),
//                new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 7),
//                new BuildingRequest(BuildingEnum.roboticsFactory, 2),
//                new BuildingRequest(BuildingEnum.shipyard, 2)//,
////                new BuildingRequest(BuildingEnum.researchLaboratory, 1)
//        ));
//
//        levelUpMap.put(2L, Arrays.asList(
//                new BuildingRequest(BuildingEnum.metalStorage, 2),
//                new BuildingRequest(BuildingEnum.crystalStorage, 1),
//                new BuildingRequest(BuildingEnum.solarPlant, 12),
//                new BuildingRequest(BuildingEnum.crystalMine, 10),
//                new BuildingRequest(BuildingEnum.solarPlant, 13),
//                new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 8),
//                new BuildingRequest(BuildingEnum.crystalMine, 11),
//                new BuildingRequest(BuildingEnum.solarPlant, 14),
//                new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 10),
//                new BuildingRequest(BuildingEnum.crystalMine, 12),
//                new BuildingRequest(BuildingEnum.solarPlant, 15),
//                new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 12),
//                new BuildingRequest(BuildingEnum.crystalMine, 13)));//,
// po co wiecej ??
//                // na odwal sie
//                new BuildingRequest(BuildingEnum.roboticsFactory, 4),
////                new BuildingRequest(BuildingEnum.researchLaboratory, 4),
//                new BuildingRequest(BuildingEnum.shipyard, 4),
//                new BuildingRequest(BuildingEnum.metalMine, 1)));
//
//
//        List<BuildingRequest> maxList = new ArrayList<>();
////        maxList.add(new BuildingRequest(BuildingEnum.roboticsFactory, 10));
//        for (int i = 1; i < 30; i++) {
//            maxList.add(new BuildingRequest(BuildingEnum.solarPlant, i+1));
//            maxList.add(new BuildingRequest(BuildingEnum.deuteriumSynthesizer, i));
//            maxList.add(new BuildingRequest(BuildingEnum.crystalMine, i));
////            maxList.add(new BuildingRequest(BuildingEnum.metalMine, i+3));
//        }
//        maxList.add(new BuildingRequest(BuildingEnum.naniteFactory, 1));
//        maxList.add(new BuildingRequest(BuildingEnum.roboticsFactory, 12));
//        maxList.add(new BuildingRequest(BuildingEnum.naniteFactory, 3));
//        levelUpMap.put(3L, maxList);
    }

    public void temporare() {//kolonie nie mogą mieć wiekszy poziom niż gracza
        PlayerEntity playerEntity = new PlayerEntity();
        playerEntity.energyTechnology=1L;
        playerEntity.combustionDriveTechnology=2L; //mt i lm
        //build mt and lm, after let go colony to level 2

        //lvl 2
        // zbiorniki zeby sie nie przepelnily
        // wiecej fabryk na wiecej krysztalu i deuteru
        // budowa 1 wyrzutni rakiet
        new BuildingRequest(BuildingEnum.researchLaboratory, 3);
        playerEntity.espionageTechnology=2L;
        new BuildingRequest(BuildingEnum.shipyard, 3);
        playerEntity.combustionDriveTechnology=3L;
        // sonda

        playerEntity.espionageTechnology=4L;
        playerEntity.impulseDriveTechnology=3L;
        playerEntity.astrophysicsTechnology=1L;
        playerEntity.computerTechnology=2L;
        //expedition start

        new BuildingRequest(BuildingEnum.shipyard, 4);
        // kolonizator

        playerEntity.computerTechnology=3L;
        playerEntity.astrophysicsTechnology=3L;

        playerEntity.combustionDriveTechnology=6L;
        //dt

        playerEntity.computerTechnology=4L;
        playerEntity.astrophysicsTechnology=3L;

        //


        new BuildingRequest(BuildingEnum.roboticsFactory, 4); //?
    }
}
