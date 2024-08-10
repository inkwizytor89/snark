package org.enoch.snark.instance.si.module.building.list.moon;

import org.enoch.snark.gi.types.BuildingEnum;
import org.enoch.snark.instance.si.module.building.BuildingRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.Arrays;
import java.util.List;

public class FastTeleport extends AbstractBuildingList {
    public static final String code = FastTeleport.class.getSimpleName().toLowerCase();

    public FastTeleport(String code) {
        super(code);
    }

    public List<BuildingRequest> create() {
        return Arrays.asList(
                new BuildingRequest(BuildingEnum.moonbase, 1),
                new BuildingRequest(BuildingEnum.roboticsFactory, 2),
                new BuildingRequest(BuildingEnum.moonbase, 2),
                new BuildingRequest(BuildingEnum.roboticsFactory, 4),
                new BuildingRequest(BuildingEnum.moonbase, 3),
                new BuildingRequest(BuildingEnum.roboticsFactory, 6),
                new BuildingRequest(BuildingEnum.jumpGate, 1)
        );
    }
}
