package org.enoch.snark.instance.si.module.building.list.planet;

import org.enoch.snark.gi.types.BuildingEnum;
import org.enoch.snark.instance.si.module.building.BuildingRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.List;

import static java.util.Collections.singletonList;

public class Custom extends AbstractBuildingList {
    public static final String code = Custom.class.getSimpleName().toLowerCase();

    public Custom(String code) {
        super(code);
    }

    public List<BuildingRequest> create() {
        BuildingEnum buildingEnum = BuildingEnum.valueOf(type);
        return singletonList(new BuildingRequest(buildingEnum, argLong(1)));
    }
}
