package org.enoch.snark.instance.si.module.building.list.lf;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.gi.types.BuildingEnum;
import org.enoch.snark.instance.si.module.building.BuildingRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MechaT2 extends AbstractBuildingList {
    public static final String code = MechaT2.class.getSimpleName().toLowerCase();

    public MechaT2(String code) {
        super(code);
    }

    public List<BuildingRequest> create() {
        List<BuildingRequest> buildingRequests = new ArrayList<>(new MechaT1(StringUtils.EMPTY).create());
        buildingRequests.addAll(Arrays.asList(
                new BuildingRequest(BuildingEnum.lifeformTech13101, 47),
                new BuildingRequest(BuildingEnum.lifeformTech13102, 56)
        ));
//        buildingRequests.add(new BuildingRequest(BuildingEnum.lifeformTech13103, 5));
        buildingRequests.add(new BuildingRequest(BuildingEnum.lifeformTech13106, 3));
        buildingRequests.add(new BuildingRequest(BuildingEnum.lifeformTech13104, 1));
        buildingRequests.add(new BuildingRequest(BuildingEnum.lifeformTech13108, 2));
        buildingRequests.add(new BuildingRequest(BuildingEnum.lifeformTech13109, 6));

        buildingRequests.addAll(Arrays.asList(
                new BuildingRequest(BuildingEnum.lifeformTech13107, 6),
                new BuildingRequest(BuildingEnum.lifeformTech13111, 5),
                new BuildingRequest(BuildingEnum.lifeformTech13110, 4)
        ));
        buildingRequests.add(new BuildingRequest(BuildingEnum.lifeformTech13104, 7));
        buildingRequests.add(new BuildingRequest(BuildingEnum.lifeformTech13105, 1));
        return create(buildingRequests);
    }
}
