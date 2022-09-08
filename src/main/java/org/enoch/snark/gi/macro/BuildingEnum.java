package org.enoch.snark.gi.macro;

import static org.enoch.snark.gi.macro.GIUrlBuilder.*;

public enum BuildingEnum {

    // Resources
    metalMine("metalMine", PAGE_RESOURCES),
    crystalMine("crystalMine", PAGE_RESOURCES),
    deuteriumSynthesizer("deuteriumSynthesizer", PAGE_RESOURCES),
    solarPlant("solarPlant", PAGE_RESOURCES),
    fusionPlant("fusionPlant", PAGE_RESOURCES),
    solarSatellite("solarSatellite", PAGE_RESOURCES),
    metalStorage("metalStorage", PAGE_RESOURCES),
    crystalStorage("crystalStorage", PAGE_RESOURCES),
    deuteriumStorage("deuteriumStorage", PAGE_RESOURCES),

    // Facilities
    roboticsFactory("roboticsFactory", PAGE_FACILITIES),
    shipyard("shipyard", PAGE_FACILITIES),
    researchLaboratory("researchLaboratory", PAGE_FACILITIES),
    allianceDepot("allianceDepot", PAGE_FACILITIES),
    missileSilo("missileSilo", PAGE_FACILITIES),
    naniteFactory("naniteFactory", PAGE_FACILITIES),
    terraformer("terraformer", PAGE_FACILITIES),
    repairDock("repairDock", PAGE_FACILITIES),

    // Lifeform
    lifeformTech14101("lifeformTech14101", PAGE_LIFEFORM),
    lifeformTech14102("lifeformTech14102", PAGE_LIFEFORM),
    lifeformTech14103("lifeformTech14103", PAGE_LIFEFORM),
    lifeformTech14104("lifeformTech14104", PAGE_LIFEFORM),
    lifeformTech14105("lifeformTech14105", PAGE_LIFEFORM),
    lifeformTech14106("lifeformTech14106", PAGE_LIFEFORM),
    lifeformTech14107("lifeformTech14107", PAGE_LIFEFORM),
    lifeformTech14108("lifeformTech14108", PAGE_LIFEFORM),
    lifeformTech14109("lifeformTech14109", PAGE_LIFEFORM),
    lifeformTech14110("lifeformTech14110", PAGE_LIFEFORM),
    lifeformTech14111("lifeformTech14111", PAGE_LIFEFORM),
    lifeformTech14112("lifeformTech14112", PAGE_LIFEFORM);

    private String name;
    private String page;

    BuildingEnum(String name, String page) {
        this.name = name;
        this.page = page;
    }

    public String getName() {
        return name;
    }

    public String getPage() {
        return page;
    }
}
