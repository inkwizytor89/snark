package org.enoch.snark.instance.si.module.building.list;

import org.enoch.snark.gi.types.BuildingEnum;
import org.enoch.snark.instance.si.module.building.BuildingRequest;
import org.enoch.snark.instance.si.module.building.list.lf.*;
import org.enoch.snark.instance.si.module.building.list.moon.FastTeleport;
import org.enoch.snark.instance.si.module.building.list.planet.Base;
import org.enoch.snark.instance.si.module.building.list.planet.Custom;
import org.enoch.snark.instance.si.module.building.list.planet.Mines;
import org.enoch.snark.instance.si.module.building.list.planet.Small;

import java.util.*;

public abstract class AbstractBuildingList {

    protected final String type;
    private final String[] split;

    public static List<BuildingRequest> convert(List<String> listNames) {
        List<BuildingRequest> buildList = new ArrayList<>();
        listNames.forEach(name -> buildList.addAll(convert(name.toLowerCase())));
        return buildList;
    }

    public static List<BuildingRequest> convert(String name) {
        if(name.startsWith(Small.code)) return new Small(name).create();
        if(name.startsWith(Base.code)) return new Base(name).create();
        if(name.startsWith(Mines.code)) return new Mines(name).create();
        if(name.startsWith(Custom.code)) return new Custom(name).create();

        if(name.startsWith(FastTeleport.code)) return new FastTeleport(name).create();

        if(name.startsWith(MechaT1.code)) return new MechaT1(name).create();
        if(name.startsWith(MechaT2.code)) return new MechaT2(name).create();
        if(name.startsWith(MechaT3.code)) return new MechaT3(name).create();

        if(name.startsWith(HumansT1.code)) return new HumansT1(name).create();
        if(name.startsWith(HumansT2.code)) return new HumansT2(name).create();

        if(name.startsWith(KaeleshT1.code)) return new KaeleshT1(name).create();
        if(name.startsWith(KaeleshT2.code)) return new KaeleshT2(name).create();

        if(name.startsWith(RocktalT1.code)) return new RocktalT1(name).create();
        if(name.startsWith(RocktalT2.code)) return new RocktalT2(name).create();

        throw new IllegalStateException("Unknown building list "+name);
    }

    protected AbstractBuildingList(String code) {
        split = code.split("_");
        type = argString(0);
    }

    protected String argString(int i) {
        if(split.length <= i) return null;
        return split[i];
    }

    protected Long argLong(int i) {
        String value = argString(i);
        if(value == null) return 0L;
        return Long.parseLong(value);
    }

    protected List<BuildingRequest> create(List<BuildingRequest> sourceBuildings) {
        Map<BuildingEnum, Long> currentProgress = new HashMap<>();
        sourceBuildings.forEach(buildingRequest -> currentProgress.put(buildingRequest.building, 0L));

        // max iteration as max level of source buildings
        long maxIt = sourceBuildings.stream().map(request -> request.level).max(Long::compareTo).get();
        List<BuildingRequest> list = new ArrayList<>();
        for(long i=1; i<=maxIt; i++) {
            for(BuildingRequest  request: sourceBuildings) {
                // i / maxIt - means percent of progress
                long newLevel = request.level * i / maxIt;
                // for new entry: add to list and mark in progress
                if(currentProgress.get(request.building) < newLevel) {
                    list.add(new BuildingRequest(request.building, newLevel));
                    currentProgress.put(request.building, newLevel);
                }
            }
        }
        return list;
    }

}
