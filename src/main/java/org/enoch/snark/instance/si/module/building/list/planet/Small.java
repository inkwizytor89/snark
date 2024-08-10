package org.enoch.snark.instance.si.module.building.list.planet;

import org.enoch.snark.gi.types.BuildingEnum;
import org.enoch.snark.instance.si.module.building.BuildingRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.Arrays;
import java.util.List;

public class Small extends AbstractBuildingList {
    public static final String code = Small.class.getSimpleName().toLowerCase();

    public Small(String code) {
        super(code);
    }

    public List<BuildingRequest> create() {
        return Arrays.asList(
                new BuildingRequest(BuildingEnum.solarPlant, 1),
                new BuildingRequest(BuildingEnum.metalMine, 2),
                new BuildingRequest(BuildingEnum.solarPlant, 2),
                new BuildingRequest(BuildingEnum.metalMine, 3),
                new BuildingRequest(BuildingEnum.crystalMine, 1),
                new BuildingRequest(BuildingEnum.solarPlant, 3),
                new BuildingRequest(BuildingEnum.metalMine, 4),
                new BuildingRequest(BuildingEnum.crystalMine, 2),
                new BuildingRequest(BuildingEnum.solarPlant, 4),
                new BuildingRequest(BuildingEnum.metalMine, 5),
                new BuildingRequest(BuildingEnum.crystalMine, 3),
                new BuildingRequest(BuildingEnum.solarPlant, 5),
                new BuildingRequest(BuildingEnum.metalMine, 6),
                new BuildingRequest(BuildingEnum.crystalMine, 4),
                new BuildingRequest(BuildingEnum.solarPlant, 6),
                new BuildingRequest(BuildingEnum.metalMine, 7),
                new BuildingRequest(BuildingEnum.crystalMine, 5),
                new BuildingRequest(BuildingEnum.solarPlant, 7),
                new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 3),
                new BuildingRequest(BuildingEnum.solarPlant, 8),
                new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 5),
                new BuildingRequest(BuildingEnum.solarPlant, 9),


                new BuildingRequest(BuildingEnum.metalStorage, 1),
                new BuildingRequest(BuildingEnum.crystalStorage, 1),
                new BuildingRequest(BuildingEnum.deuteriumStorage, 1),

                new BuildingRequest(BuildingEnum.roboticsFactory, 2),
//                new BuildingRequest(BuildingEnum.researchLaboratory, 1),
                new BuildingRequest(BuildingEnum.shipyard, 2),

                new BuildingRequest(BuildingEnum.metalMine, 8),
                new BuildingRequest(BuildingEnum.crystalMine, 6),
                new BuildingRequest(BuildingEnum.metalMine, 9),
                new BuildingRequest(BuildingEnum.solarPlant, 10),
                new BuildingRequest(BuildingEnum.crystalMine, 7),
                new BuildingRequest(BuildingEnum.metalMine, 10),
                new BuildingRequest(BuildingEnum.crystalMine, 8),
                new BuildingRequest(BuildingEnum.solarPlant, 11),

                new BuildingRequest(BuildingEnum.metalMine, 11),
                new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 7),
                new BuildingRequest(BuildingEnum.solarPlant, 12),
                new BuildingRequest(BuildingEnum.metalMine, 12),
                new BuildingRequest(BuildingEnum.crystalMine, 10),
                new BuildingRequest(BuildingEnum.solarPlant, 13),

                new BuildingRequest(BuildingEnum.metalMine, 13),
                new BuildingRequest(BuildingEnum.crystalMine, 11),
                new BuildingRequest(BuildingEnum.solarPlant, 14),

                new BuildingRequest(BuildingEnum.metalMine, 14),
                new BuildingRequest(BuildingEnum.crystalMine, 12),
                new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 10),

                new BuildingRequest(BuildingEnum.metalStorage, 2),
                new BuildingRequest(BuildingEnum.solarPlant, 15),

                new BuildingRequest(BuildingEnum.metalStorage, 3),
                new BuildingRequest(BuildingEnum.deuteriumStorage, 2),
                new BuildingRequest(BuildingEnum.crystalStorage, 2),
                new BuildingRequest(BuildingEnum.solarPlant, 16),

                new BuildingRequest(BuildingEnum.metalStorage, 4),
                new BuildingRequest(BuildingEnum.crystalStorage, 3),
                new BuildingRequest(BuildingEnum.deuteriumSynthesizer, 12),
                new BuildingRequest(BuildingEnum.crystalMine, 13),
                new BuildingRequest(BuildingEnum.roboticsFactory, 4),
                new BuildingRequest(BuildingEnum.shipyard, 4)
        );
    }
}
