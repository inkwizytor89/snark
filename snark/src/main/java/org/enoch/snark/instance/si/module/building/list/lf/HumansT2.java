package org.enoch.snark.instance.si.module.building.list.lf;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.instance.model.technology.Building;
import org.enoch.snark.instance.model.technology.LFBuilding;
import org.enoch.snark.instance.si.module.building.BuildRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HumansT2 extends AbstractBuildingList {
    public static final String code = HumansT2.class.getSimpleName().toLowerCase();

    public HumansT2(String code) {
        super(code);
    }

    public List<BuildRequest> create() {
        List<BuildRequest> buildRequests = new ArrayList<>(new HumansT1(StringUtils.EMPTY).create());
        buildRequests.addAll(Arrays.asList(
                new BuildRequest(LFBuilding.lifeformTech11101, 41),
                new BuildRequest(LFBuilding.lifeformTech11102, 43),
                new BuildRequest(LFBuilding.lifeformTech11104, 1)
        ));
        return create(buildRequests);
    }
}
