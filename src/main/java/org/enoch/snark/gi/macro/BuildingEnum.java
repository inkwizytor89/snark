package org.enoch.snark.gi.macro;

import static org.enoch.snark.gi.macro.GIUrl.*;
import static org.enoch.snark.gi.macro.UrlComponent.*;

public enum BuildingEnum {

    // Resources
    metalMine,
    crystalMine,
    deuteriumSynthesizer,
    solarPlant,
    fusionPlant,
    solarSatellite,
    metalStorage,
    crystalStorage,
    deuteriumStorage,

    // Facilities
    roboticsFactory,
    shipyard,
    researchLaboratory,
    allianceDepot,
    missileSilo,
    naniteFactory,
    terraformer,
    repairDock,

    // Lifeform

    // Humans
    lifeformTech11101,
    lifeformTech11102,
    lifeformTech11103,
    lifeformTech11104,
    lifeformTech11105,
    lifeformTech11106,
    lifeformTech11107,
    lifeformTech11108,
    lifeformTech11109,
    lifeformTech11110,
    lifeformTech11111,
    lifeformTech11112,

    // Rocktal
    lifeformTech12101,
    lifeformTech12102,
    lifeformTech12103,
    lifeformTech12104,
    lifeformTech12105,
    lifeformTech12106,
    lifeformTech12107,
    lifeformTech12108,
    lifeformTech12109,
    lifeformTech12110,
    lifeformTech12111,
    lifeformTech12112,

    // Mecha
    lifeformTech13101,
    lifeformTech13102,
    lifeformTech13103,
    lifeformTech13104,
    lifeformTech13105,
    lifeformTech13106,
    lifeformTech13107,
    lifeformTech13108,
    lifeformTech13109,
    lifeformTech13110,
    lifeformTech13111,
    lifeformTech13112,
    //Kaelesh
    lifeformTech14101,
    lifeformTech14102,
    lifeformTech14103,
    lifeformTech14104,
    lifeformTech14105,
    lifeformTech14106,
    lifeformTech14107,
    lifeformTech14108,
    lifeformTech14109,
    lifeformTech14110,
    lifeformTech14111,
    lifeformTech14112;

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
                return FACILITIES;
            case lifeformTech11101:
            case lifeformTech11102:
            case lifeformTech11103:
            case lifeformTech11104:
            case lifeformTech11105:
            case lifeformTech11106:
            case lifeformTech11107:
            case lifeformTech11108:
            case lifeformTech11109:
            case lifeformTech11110:
            case lifeformTech11111:
            case lifeformTech11112:

            case lifeformTech12101:
            case lifeformTech12102:
            case lifeformTech12103:
            case lifeformTech12104:
            case lifeformTech12105:
            case lifeformTech12106:
            case lifeformTech12107:
            case lifeformTech12108:
            case lifeformTech12109:
            case lifeformTech12110:
            case lifeformTech12111:
            case lifeformTech12112:

            case lifeformTech13101:
            case lifeformTech13102:
            case lifeformTech13103:
            case lifeformTech13104:
            case lifeformTech13105:
            case lifeformTech13106:
            case lifeformTech13107:
            case lifeformTech13108:
            case lifeformTech13109:
            case lifeformTech13110:
            case lifeformTech13111:
            case lifeformTech13112:

            case lifeformTech14101:
            case lifeformTech14102:
            case lifeformTech14103:
            case lifeformTech14104:
            case lifeformTech14105:
            case lifeformTech14106:
            case lifeformTech14107:
            case lifeformTech14108:
            case lifeformTech14109:
            case lifeformTech14110:
            case lifeformTech14111:
            case lifeformTech14112:
                return LFBUILDINGS;
            default: throw new RuntimeException("Can not find component for " + this.name());
        }
    }
}
