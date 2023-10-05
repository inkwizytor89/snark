package org.enoch.snark.module.building;

import static org.enoch.snark.model.types.LifeFormType.HUMAN;
import static org.enoch.snark.model.types.LifeFormType.KAELESH;
import static org.enoch.snark.model.types.LifeFormType.MECHA;
import static org.enoch.snark.model.types.LifeFormType.ROCKTAL;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.macro.BuildingEnum;
import org.enoch.snark.instance.Instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormsManager {

    public BuildingRequest getBuildRequest(ColonyEntity colony) {
        final Map<Long, List<BuildingRequest>> levelUpMap = generateLevelUpMap(colony);
        if(getLevel(colony) > levelUpMap.size()) {
            System.err.println(colony+" reach max level " + getLevel(colony));
            return null;
        }
        if (!levelUpMap.containsKey(colony.formsLevel)) {
            return null;
        }
        for(BuildingRequest request : levelUpMap.get(getLevel(colony))) {
            Long buildingLevel = colony.getBuildingLevel(request.building);
            if(buildingLevel == null || request.level > buildingLevel) {
                return request;
            }
        }
        setLevel(colony, getLevel(colony) +1);
        return getBuildRequest(colony);
    }

    private static void setLevel(final ColonyEntity colony, Long level) {
        colony.formsLevel = level;
    }

    private static Long getLevel(final ColonyEntity colony) {
        return colony.formsLevel;
    }

    private Map<Long, List<BuildingRequest>> generateLevelUpMap(final ColonyEntity colony) {
        final Map<Long, List<BuildingRequest>> map = new HashMap<>();
        final String lfType = Instance.config.getConfig(FormsThread.threadName, FormsThread.TYPE, KAELESH.name());
        if(HUMAN.equalsTo(lfType)) {
            buildHumans(map);
        } else if(ROCKTAL.equalsTo(lfType)) {
            buildRocktal(map);
        } else if(MECHA.equalsTo(lfType)) {
            buildMechas(map);
        } else if(KAELESH.equalsTo(lfType)) {
            buildKaelesh(map);
        }
        return map;
    }

    private void buildHumans(final Map<Long, List<BuildingRequest>> map) {
        final ArrayList<BuildingRequest> list = new ArrayList<>();
        for(long i=2; i<=22; i++) {
            list.add(new BuildingRequest(BuildingEnum.lifeformTech11101, i-1));
            list.add(new BuildingRequest(BuildingEnum.lifeformTech11102, i));
        }
        list.add(new BuildingRequest(BuildingEnum.lifeformTech11103, 1));
        map.put(1L, list);

        final ArrayList<BuildingRequest> listLvl2 = new ArrayList<>();
        for(long i=22; i<=41; i++) {
            listLvl2.add(new BuildingRequest(BuildingEnum.lifeformTech11101, i));
            listLvl2.add(new BuildingRequest(BuildingEnum.lifeformTech11102, i+2));
        }
        listLvl2.add(new BuildingRequest(BuildingEnum.lifeformTech11104, 1));
        map.put(2L, listLvl2);
    }

    private void buildRocktal(final Map<Long, List<BuildingRequest>> map) {
        final ArrayList<BuildingRequest> list = new ArrayList<>();
        for(long i=2; i<=22; i++) {
            list.add(new BuildingRequest(BuildingEnum.lifeformTech12101, i-1));
            list.add(new BuildingRequest(BuildingEnum.lifeformTech12102, i));
        }
        list.add(new BuildingRequest(BuildingEnum.lifeformTech12103, 1));
        map.put(1L, list);

        final ArrayList<BuildingRequest> listLvl2 = new ArrayList<>();
        for(long i=22; i<=41; i++) {
            listLvl2.add(new BuildingRequest(BuildingEnum.lifeformTech12101, i));
            listLvl2.add(new BuildingRequest(BuildingEnum.lifeformTech12102, i+2));
        }
        listLvl2.add(new BuildingRequest(BuildingEnum.lifeformTech12104, 1));
        map.put(2L, listLvl2);
    }

    private void buildMechas(final Map<Long, List<BuildingRequest>> map) {
        final ArrayList<BuildingRequest> list = new ArrayList<>();
        for(long i=2; i<=22; i++) {
            list.add(new BuildingRequest(BuildingEnum.lifeformTech13101, i-1));
            list.add(new BuildingRequest(BuildingEnum.lifeformTech13102, i));
        }
        list.add(new BuildingRequest(BuildingEnum.lifeformTech13103, 1));
        map.put(1L, list);

        final ArrayList<BuildingRequest> listLvl2 = new ArrayList<>();
        for(long i=22; i<=41; i++) {
            listLvl2.add(new BuildingRequest(BuildingEnum.lifeformTech13101, i));
            listLvl2.add(new BuildingRequest(BuildingEnum.lifeformTech13102, i+2));
        }
        listLvl2.add(new BuildingRequest(BuildingEnum.lifeformTech13104, 1));
        map.put(2L, listLvl2);
    }

    private void buildKaelesh(final Map<Long, List<BuildingRequest>> map) {
        final ArrayList<BuildingRequest> listLvl1 = new ArrayList<>();
        for(long i=2; i<=22; i++) {
            listLvl1.add(new BuildingRequest(BuildingEnum.lifeformTech14101, i-1));
            listLvl1.add(new BuildingRequest(BuildingEnum.lifeformTech14102, i));
        }
        listLvl1.add(new BuildingRequest(BuildingEnum.lifeformTech14103, 1));
        map.put(1L, listLvl1);

        final ArrayList<BuildingRequest> listLvl2 = new ArrayList<>();
        for(long i=22; i<=41; i++) {
            listLvl2.add(new BuildingRequest(BuildingEnum.lifeformTech14101, i));
            listLvl2.add(new BuildingRequest(BuildingEnum.lifeformTech14102, i+2));
        }
        listLvl2.add(new BuildingRequest(BuildingEnum.lifeformTech14104, 1));
        map.put(2L, listLvl2);
    }
}
