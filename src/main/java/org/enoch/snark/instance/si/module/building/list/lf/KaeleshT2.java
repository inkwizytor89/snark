package org.enoch.snark.instance.si.module.building.list.lf;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.gi.types.BuildingEnum;
import org.enoch.snark.instance.si.module.building.BuildingRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KaeleshT2 extends AbstractBuildingList {
    public static final String code = KaeleshT2.class.getSimpleName().toLowerCase();

    public KaeleshT2(String code) {
        super(code);
    }

    public List<BuildingRequest> create() {
        List<BuildingRequest> buildingRequests = new ArrayList<>(new KaeleshT1(StringUtils.EMPTY).create());
        buildingRequests.addAll(Arrays.asList(
                new BuildingRequest(BuildingEnum.lifeformTech14101, 41),
                new BuildingRequest(BuildingEnum.lifeformTech14102, 43),
                new BuildingRequest(BuildingEnum.lifeformTech14104, 1)
        ));
        return buildingRequests;
    }
}
