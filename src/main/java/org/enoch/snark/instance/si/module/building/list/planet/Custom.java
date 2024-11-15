package org.enoch.snark.instance.si.module.building.list.planet;

import org.enoch.snark.instance.model.technology.Building;
import org.enoch.snark.instance.si.module.building.BuildRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.List;

import static java.util.Collections.singletonList;

public class Custom extends AbstractBuildingList {
    public static final String code = Custom.class.getSimpleName().toLowerCase();

    public Custom(String code) {
        super(code);
    }

    public List<BuildRequest> create() {
        Building building = Building.valueOf(type);
        return singletonList(new BuildRequest(building, argLong(1)));
    }
}
