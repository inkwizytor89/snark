package org.enoch.snark.instance.si.module.building.list.planet;

import org.enoch.snark.instance.model.technology.Building;
import org.enoch.snark.instance.si.module.building.BuildRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.Arrays;
import java.util.List;

public class DirectiveIV extends AbstractBuildingList {
    public static final String code = DirectiveIV.class.getSimpleName().toLowerCase();

    public DirectiveIV(String code) {
        super(code);
    }

    public List<BuildRequest> create() {
        return Arrays.asList(
                new BuildRequest(Building.solarPlant, 1),
                new BuildRequest(Building.metalMine, 2),
                new BuildRequest(Building.solarPlant, 2),
                new BuildRequest(Building.metalMine, 3),
                new BuildRequest(Building.crystalMine, 1),
                new BuildRequest(Building.solarPlant, 3),
                new BuildRequest(Building.metalMine, 4),
                new BuildRequest(Building.crystalMine, 2),
                new BuildRequest(Building.solarPlant, 4),
                new BuildRequest(Building.metalMine, 5),
                new BuildRequest(Building.crystalMine, 3),
                new BuildRequest(Building.solarPlant, 5),
                new BuildRequest(Building.metalMine, 6),
                new BuildRequest(Building.crystalMine, 4),
                new BuildRequest(Building.solarPlant, 6),
                new BuildRequest(Building.metalMine, 7),
                new BuildRequest(Building.crystalMine, 5),
                new BuildRequest(Building.solarPlant, 7),
                new BuildRequest(Building.deuteriumSynthesizer, 3),
                new BuildRequest(Building.solarPlant, 8),
                new BuildRequest(Building.deuteriumSynthesizer, 5),
                new BuildRequest(Building.solarPlant, 9),


                new BuildRequest(Building.metalStorage, 1),
                new BuildRequest(Building.crystalStorage, 1),
                new BuildRequest(Building.deuteriumStorage, 1),

                new BuildRequest(Building.roboticsFactory, 2),
//                new BuildingRequest(BuildingEnum.researchLaboratory, 1),
                new BuildRequest(Building.shipyard, 2),

                new BuildRequest(Building.metalMine, 8),
                new BuildRequest(Building.crystalMine, 6),
                new BuildRequest(Building.metalMine, 9),
                new BuildRequest(Building.solarPlant, 10),
                new BuildRequest(Building.crystalMine, 7),
                new BuildRequest(Building.metalMine, 10),
                new BuildRequest(Building.crystalMine, 8),
                new BuildRequest(Building.solarPlant, 11),

                new BuildRequest(Building.metalMine, 11),
                new BuildRequest(Building.deuteriumSynthesizer, 7),
                new BuildRequest(Building.solarPlant, 12),
                new BuildRequest(Building.metalMine, 12),
                new BuildRequest(Building.crystalMine, 10),
                new BuildRequest(Building.solarPlant, 13),

                new BuildRequest(Building.metalMine, 13),
                new BuildRequest(Building.crystalMine, 11),
                new BuildRequest(Building.solarPlant, 14),

                new BuildRequest(Building.metalMine, 14),
                new BuildRequest(Building.crystalMine, 12),
                new BuildRequest(Building.deuteriumSynthesizer, 10),


                new BuildRequest(Building.metalStorage, 2),
                new BuildRequest(Building.solarPlant, 15),

                new BuildRequest(Building.crystalMine, 13),
                new BuildRequest(Building.shipyard, 4)
        );
    }
}
