package org.enoch.snark.instance.si.module.building.list.lf;

import org.enoch.snark.instance.model.technology.Building;
import org.enoch.snark.instance.model.technology.LFBuilding;
import org.enoch.snark.instance.si.module.building.BuildRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.Arrays;
import java.util.List;

public class KaeleshT1 extends AbstractBuildingList {
    public static final String code = KaeleshT1.class.getSimpleName().toLowerCase();

    public KaeleshT1(String code) {
        super(code);
    }

    public List<BuildRequest> create() {
        List<BuildRequest> sourceList = Arrays.asList(
                new BuildRequest(LFBuilding.lifeformTech14101, 21),
                new BuildRequest(LFBuilding.lifeformTech14102, 22),
                new BuildRequest(LFBuilding.lifeformTech14103, 1)
        );
        return create(sourceList);
    }
}
