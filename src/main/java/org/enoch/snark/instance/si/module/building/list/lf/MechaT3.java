package org.enoch.snark.instance.si.module.building.list.lf;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.gi.types.BuildingEnum;
import org.enoch.snark.instance.si.module.building.BuildingRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MechaT3 extends AbstractBuildingList {
    public static final String code = MechaT3.class.getSimpleName().toLowerCase();

    public MechaT3(String code) {
        super(code);
    }

    public List<BuildingRequest> create() {
        List<BuildingRequest> buildingRequests = new ArrayList<>(new MechaT2(StringUtils.EMPTY).create());
        buildingRequests.addAll(Arrays.asList(
                new BuildingRequest(BuildingEnum.lifeformTech13101, 73),
                new BuildingRequest(BuildingEnum.lifeformTech13102, 85)
        ));
        buildingRequests.addAll(Arrays.asList(
                new BuildingRequest(BuildingEnum.lifeformTech13108, 23),
                new BuildingRequest(BuildingEnum.lifeformTech13109, 14)
        ));

        buildingRequests.addAll(Arrays.asList(
                new BuildingRequest(BuildingEnum.lifeformTech13104, 13),
                new BuildingRequest(BuildingEnum.lifeformTech13105, 9)
        ));
        return create(buildingRequests);
    }
}
