package org.enoch.snark.instance.si.module.building.list.lf;

import org.enoch.snark.gi.types.BuildingEnum;
import org.enoch.snark.instance.si.module.building.BuildingRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.Arrays;
import java.util.List;

public class KaeleshT1 extends AbstractBuildingList {
    public static final String code = KaeleshT1.class.getSimpleName().toLowerCase();

    public KaeleshT1(String code) {
        super(code);
    }

    public List<BuildingRequest> create() {
        List<BuildingRequest> sourceList = Arrays.asList(
                new BuildingRequest(BuildingEnum.lifeformTech14101, 21),
                new BuildingRequest(BuildingEnum.lifeformTech14102, 22),
                new BuildingRequest(BuildingEnum.lifeformTech14103, 1)
        );
        return create(sourceList);
    }
}
