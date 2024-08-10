package org.enoch.snark.instance.si.module.building.list.lf;

import org.enoch.snark.gi.types.BuildingEnum;
import org.enoch.snark.instance.si.module.building.BuildingRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.Arrays;
import java.util.List;

public class MechaT1 extends AbstractBuildingList {
    public static final String code = MechaT1.class.getSimpleName().toLowerCase();

    public MechaT1(String code) {
        super(code);
    }

    public List<BuildingRequest> create() {
        List<BuildingRequest> sourceList = Arrays.asList(
                new BuildingRequest(BuildingEnum.lifeformTech13101, 17),
                new BuildingRequest(BuildingEnum.lifeformTech13102, 20),
                new BuildingRequest(BuildingEnum.lifeformTech13103, 1)
        );
        return create(sourceList);
    }
}
