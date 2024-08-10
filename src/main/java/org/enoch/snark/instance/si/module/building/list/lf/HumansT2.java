package org.enoch.snark.instance.si.module.building.list.lf;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.gi.types.BuildingEnum;
import org.enoch.snark.instance.si.module.building.BuildingRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HumansT2 extends AbstractBuildingList {
    public static final String code = HumansT2.class.getSimpleName().toLowerCase();

    public HumansT2(String code) {
        super(code);
    }

    public List<BuildingRequest> create() {
        List<BuildingRequest> buildingRequests = new ArrayList<>(new HumansT1(StringUtils.EMPTY).create());
        buildingRequests.addAll(Arrays.asList(
                new BuildingRequest(BuildingEnum.lifeformTech11101, 41),
                new BuildingRequest(BuildingEnum.lifeformTech11102, 43),
                new BuildingRequest(BuildingEnum.lifeformTech11104, 1)
        ));
        return buildingRequests;
    }
}
