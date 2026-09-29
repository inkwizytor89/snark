package org.enoch.snark.instance.si.module.building.list.lf;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.instance.model.technology.Building;
import org.enoch.snark.instance.model.technology.LFBuilding;
import org.enoch.snark.instance.si.module.building.BuildRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MechaT2 extends AbstractBuildingList {
    public static final String code = MechaT2.class.getSimpleName().toLowerCase();

    public MechaT2(String code) {
        super(code);
    }

    public List<BuildRequest> create() {
        List<BuildRequest> buildRequests = new ArrayList<>(new MechaT1(StringUtils.EMPTY).create());
        buildRequests.addAll(Arrays.asList(
                new BuildRequest(LFBuilding.lifeformTech13101, 47),
                new BuildRequest(LFBuilding.lifeformTech13102, 56)
        ));
//        buildingRequests.add(new BuildingRequest(BuildingEnum.lifeformTech13103, 5));
        buildRequests.add(new BuildRequest(LFBuilding.lifeformTech13106, 3));
        buildRequests.add(new BuildRequest(LFBuilding.lifeformTech13104, 1));
        buildRequests.add(new BuildRequest(LFBuilding.lifeformTech13108, 2));
        buildRequests.add(new BuildRequest(LFBuilding.lifeformTech13109, 6));

        buildRequests.addAll(Arrays.asList(
                new BuildRequest(LFBuilding.lifeformTech13107, 6),
                new BuildRequest(LFBuilding.lifeformTech13111, 5),
                new BuildRequest(LFBuilding.lifeformTech13110, 4)
        ));
        buildRequests.add(new BuildRequest(LFBuilding.lifeformTech13104, 7));
        buildRequests.add(new BuildRequest(LFBuilding.lifeformTech13105, 1));
        return create(buildRequests);
    }
}
