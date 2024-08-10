package org.enoch.snark.instance.si.module.building.list.planet;

import org.enoch.snark.gi.types.BuildingEnum;
import org.enoch.snark.instance.si.module.building.BuildingRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.Arrays;
import java.util.List;

public class Base extends AbstractBuildingList {
    public static final String code = Base.class.getSimpleName().toLowerCase();

    public Base(String code) {
        super(code);
    }

    public List<BuildingRequest> create() {
        List<BuildingRequest> sourceList = Arrays.asList(
                new BuildingRequest(BuildingEnum.metalMine, 27),
                new BuildingRequest(BuildingEnum.crystalMine, 23),
                new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 24),
                new BuildingRequest(BuildingEnum.solarPlant, 32),
                new BuildingRequest(BuildingEnum.metalStorage, 12),
                new BuildingRequest(BuildingEnum.crystalStorage, 11),
                new BuildingRequest(BuildingEnum.deuteriumStorage, 10),
                new BuildingRequest(BuildingEnum.metalStorage, -1)
        );
        return create(sourceList);
    }
}
