package org.enoch.snark.instance.si.module.building.list.planet;

import org.enoch.snark.instance.model.technology.Building;
import org.enoch.snark.instance.si.module.building.BuildRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.Arrays;
import java.util.List;

public class Mines extends AbstractBuildingList {
    public static final String code = Mines.class.getSimpleName().toLowerCase();

    public Mines(String code) {
        super(code);
    }

    public List<BuildRequest> create() {
        List<BuildRequest> sourceList = Arrays.asList(
                new BuildRequest(Building.metalMine, this.argLong(1)),
                new BuildRequest(Building.crystalMine, this.argLong(2)),
                new BuildRequest(Building.deuteriumSynthesizer, this.argLong(3)),
                new BuildRequest(Building.solarPlant, this.argLong(4)),
                new BuildRequest(Building.fusionPlant, this.argLong(5))
        );
        return create(sourceList);
    }
}
