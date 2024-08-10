package org.enoch.snark.instance.si.module.building.list.lf;

import org.enoch.snark.gi.types.BuildingEnum;
import org.enoch.snark.instance.si.module.building.BuildingRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.Arrays;
import java.util.List;

public class HumansT1 extends AbstractBuildingList {
    public static final String code = HumansT1.class.getSimpleName().toLowerCase();

    public HumansT1(String code) {
        super(code);
    }

    public List<BuildingRequest> create() {
        List<BuildingRequest> sourceList = Arrays.asList(
                new BuildingRequest(BuildingEnum.lifeformTech11101, 21),
                new BuildingRequest(BuildingEnum.lifeformTech11102, 22),
                new BuildingRequest(BuildingEnum.lifeformTech11103, 1)
        );
        return create(sourceList);
    }
}
