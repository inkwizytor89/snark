package org.enoch.snark.instance.si.module.building.list.lf;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.instance.model.technology.Building;
import org.enoch.snark.instance.model.technology.LFBuilding;
import org.enoch.snark.instance.si.module.building.BuildRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MechaT3 extends AbstractBuildingList {
    public static final String code = MechaT3.class.getSimpleName().toLowerCase();

    public MechaT3(String code) {
        super(code);
    }

    public List<BuildRequest> create() {
        List<BuildRequest> buildRequests = new ArrayList<>(new MechaT2(StringUtils.EMPTY).create());
        buildRequests.addAll(Arrays.asList(
                new BuildRequest(LFBuilding.lifeformTech13101, 73),
                new BuildRequest(LFBuilding.lifeformTech13102, 85)
        ));
        buildRequests.addAll(Arrays.asList(
                new BuildRequest(LFBuilding.lifeformTech13108, 23),
                new BuildRequest(LFBuilding.lifeformTech13109, 14)
        ));

        buildRequests.addAll(Arrays.asList(
                new BuildRequest(LFBuilding.lifeformTech13104, 13),
                new BuildRequest(LFBuilding.lifeformTech13105, 9)
        ));
        return create(buildRequests);
    }
}
