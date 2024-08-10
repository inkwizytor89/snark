package org.enoch.snark.instance.si.module.building.list.lf;

import org.enoch.snark.gi.types.BuildingEnum;
import org.enoch.snark.instance.si.module.building.BuildingRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.Arrays;
import java.util.List;

public class RocktalT1 extends AbstractBuildingList {
    public static final String code = RocktalT1.class.getSimpleName().toLowerCase();

    public RocktalT1(String code) {
        super(code);
    }

    public List<BuildingRequest> create() {
        List<BuildingRequest> sourceList = Arrays.asList(
                new BuildingRequest(BuildingEnum.lifeformTech12101, 21),
                new BuildingRequest(BuildingEnum.lifeformTech12102, 22),
                new BuildingRequest(BuildingEnum.lifeformTech12103, 1)
        );
        return create(sourceList);
    }
}
