package org.enoch.snark.instance.si.module.building.list.planet;

import org.enoch.snark.gi.types.BuildingEnum;
import org.enoch.snark.instance.si.module.building.BuildingRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.Arrays;
import java.util.List;

public class Mines extends AbstractBuildingList {
    public static final String code = Mines.class.getSimpleName().toLowerCase();

    public Mines(String code) {
        super(code);
    }

    public List<BuildingRequest> create() {
        List<BuildingRequest> sourceList = Arrays.asList(
                new BuildingRequest(BuildingEnum.metalMine, this.argLong(1)),
                new BuildingRequest(BuildingEnum.crystalMine, this.argLong(2)),
                new BuildingRequest(BuildingEnum.deuteriumSynthesizer, this.argLong(3)),
                new BuildingRequest(BuildingEnum.solarPlant, this.argLong(4)),
                new BuildingRequest(BuildingEnum.fusionPlant, this.argLong(5))
        );
        return create(sourceList);
    }
}
