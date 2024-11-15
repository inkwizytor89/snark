package org.enoch.snark.instance.si.module.building.list.planet;

import org.enoch.snark.instance.model.technology.Building;
import org.enoch.snark.instance.si.module.building.BuildRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.Arrays;
import java.util.List;

public class Base extends AbstractBuildingList {
    public static final String code = Base.class.getSimpleName().toLowerCase();

    public Base(String code) {
        super(code);
    }

    public List<BuildRequest> create() {
        List<BuildRequest> sourceList = Arrays.asList(
                new BuildRequest(Building.metalMine, 27),
                new BuildRequest(Building.crystalMine, 23),
                new BuildRequest(Building.deuteriumSynthesizer, 24),
                new BuildRequest(Building.solarPlant, 32),
                new BuildRequest(Building.metalStorage, 12),
                new BuildRequest(Building.crystalStorage, 11),
                new BuildRequest(Building.deuteriumStorage, 10),
                new BuildRequest(Building.metalStorage, -1)
        );
        return create(sourceList);
    }
}
