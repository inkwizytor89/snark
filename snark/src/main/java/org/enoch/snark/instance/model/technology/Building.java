package org.enoch.snark.instance.model.technology;

import lombok.Getter;
import org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent;

import java.util.Arrays;
import java.util.List;

import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FACILITIES;
import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.SUPPLIES;

@Getter
public enum Building implements Technology {

    // Resources
    metalMine(1L),
    crystalMine(2L),
    deuteriumSynthesizer(3L),
    solarPlant(4L),
    fusionPlant(12L),
    solarSatellite(212L),
    metalStorage(22L),
    crystalStorage(23L),
    deuteriumStorage(24L),

    // Facilities
    roboticsFactory(14L),
    shipyard(21L),
    researchLaboratory(31L),
    allianceDepot(34L),
    missileSilo(44L),
    naniteFactory(15L),
    terraformer(33L),
    repairDock(36L),

    moonbase(41L),
    sensorPhalanx(42L),
    jumpGate(43L);

    private final Long id;

    Building(Long id) {
        this.id = id;
    }

    public static List<Building> baseBuildings() {
        return Arrays.stream(Building.values())
                .filter(buildingEnum -> !buildingEnum.isLifeform())
                .toList();
    }

    public boolean isLifeform() {
        return name().contains("lifeform");
    }

    public UrlComponent getPage() {
        switch (this) {
            case metalMine :
            case crystalMine:
            case deuteriumSynthesizer:
            case solarPlant:
            case fusionPlant:
            case solarSatellite:
            case metalStorage:
            case crystalStorage:
            case deuteriumStorage:
                return SUPPLIES;
            case roboticsFactory:
            case shipyard:
            case researchLaboratory:
            case allianceDepot:
            case missileSilo:
            case naniteFactory:
            case terraformer:
            case repairDock:
            case moonbase:
            case sensorPhalanx:
            case jumpGate:
                return FACILITIES;
            default: throw new RuntimeException("Can not find component for " + this.name());
        }
    }
}
