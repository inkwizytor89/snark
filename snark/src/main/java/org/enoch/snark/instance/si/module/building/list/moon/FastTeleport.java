package org.enoch.snark.instance.si.module.building.list.moon;

import org.enoch.snark.instance.model.technology.Building;
import org.enoch.snark.instance.si.module.building.BuildRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.Arrays;
import java.util.List;

public class FastTeleport extends AbstractBuildingList {
    public static final String code = FastTeleport.class.getSimpleName().toLowerCase();

    public FastTeleport(String code) {
        super(code);
    }

    public List<BuildRequest> create() {
        return Arrays.asList(
                new BuildRequest(Building.moonbase, 1),
                new BuildRequest(Building.roboticsFactory, 2),
                new BuildRequest(Building.moonbase, 2),
                new BuildRequest(Building.roboticsFactory, 4),
                new BuildRequest(Building.moonbase, 3),
                new BuildRequest(Building.roboticsFactory, 6),
                new BuildRequest(Building.jumpGate, 1)
        );
    }
}
