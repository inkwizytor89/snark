package org.enoch.snark.instance.si.module.building.list.lf;

import org.enoch.snark.instance.model.technology.Building;
import org.enoch.snark.instance.model.technology.LFBuilding;
import org.enoch.snark.instance.si.module.building.BuildRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.Arrays;
import java.util.List;

public class MechaT1 extends AbstractBuildingList {
    public static final String code = MechaT1.class.getSimpleName().toLowerCase();

    public MechaT1(String code) {
        super(code);
    }

    public List<BuildRequest> create() {
        List<BuildRequest> sourceList = Arrays.asList(
                new BuildRequest(LFBuilding.lifeformTech13101, 17),
                new BuildRequest(LFBuilding.lifeformTech13102, 20),
                new BuildRequest(LFBuilding.lifeformTech13103, 1)
        );
        return create(sourceList);
    }
}
