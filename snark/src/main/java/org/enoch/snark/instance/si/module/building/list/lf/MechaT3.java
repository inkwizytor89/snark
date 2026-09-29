package org.enoch.snark.instance.si.module.building.list.lf;

import org.apache.commons.lang3.StringUtils;
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

        buildRequests.addAll(create(Arrays.asList(
                new BuildRequest(LFBuilding.lifeformTech13101, 72),
                new BuildRequest(LFBuilding.lifeformTech13102, 83),
                new BuildRequest(LFBuilding.lifeformTech13108, 30),
                new BuildRequest(LFBuilding.lifeformTech13109, 22),
                new BuildRequest(LFBuilding.lifeformTech13104, 12),
                new BuildRequest(LFBuilding.lifeformTech13105, 7)
        )));

        // t2 and t3 buildings requires population and sometimes list is blocked by this reason,
        // that is why is divided on second list on end processing
        buildRequests.addAll(create(Arrays.asList(
                new BuildRequest(LFBuilding.lifeformTech13104, 14),
                new BuildRequest(LFBuilding.lifeformTech13105, 9)
        )));
        return buildRequests;
    }
}
