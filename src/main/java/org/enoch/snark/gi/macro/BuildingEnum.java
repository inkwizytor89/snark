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

    // Humans
    lifeformTech11101("lifeformTech11101", PAGE_LIFEFORM),
    lifeformTech11102("lifeformTech11102", PAGE_LIFEFORM),
    lifeformTech11103("lifeformTech11103", PAGE_LIFEFORM),
    lifeformTech11104("lifeformTech11104", PAGE_LIFEFORM),
    lifeformTech11105("lifeformTech11105", PAGE_LIFEFORM),
    lifeformTech11106("lifeformTech11106", PAGE_LIFEFORM),
    lifeformTech11107("lifeformTech11107", PAGE_LIFEFORM),
    lifeformTech11108("lifeformTech11108", PAGE_LIFEFORM),
    lifeformTech11109("lifeformTech11109", PAGE_LIFEFORM),
    lifeformTech11110("lifeformTech11110", PAGE_LIFEFORM),
    lifeformTech11111("lifeformTech11111", PAGE_LIFEFORM),
    lifeformTech11112("lifeformTech11112", PAGE_LIFEFORM),

    // Rocktal
    lifeformTech12101("lifeformTech12101", PAGE_LIFEFORM),
    lifeformTech12102("lifeformTech12102", PAGE_LIFEFORM),
    lifeformTech12103("lifeformTech12103", PAGE_LIFEFORM),
    lifeformTech12104("lifeformTech12104", PAGE_LIFEFORM),
    lifeformTech12105("lifeformTech12105", PAGE_LIFEFORM),
    lifeformTech12106("lifeformTech12106", PAGE_LIFEFORM),
    lifeformTech12107("lifeformTech12107", PAGE_LIFEFORM),
    lifeformTech12108("lifeformTech12108", PAGE_LIFEFORM),
    lifeformTech12109("lifeformTech12109", PAGE_LIFEFORM),
    lifeformTech12110("lifeformTech12110", PAGE_LIFEFORM),
    lifeformTech12111("lifeformTech12111", PAGE_LIFEFORM),
    lifeformTech12112("lifeformTech12112", PAGE_LIFEFORM),

    // Mecha
    lifeformTech13101("lifeformTech13101", PAGE_LIFEFORM),
    lifeformTech13102("lifeformTech13102", PAGE_LIFEFORM),
    lifeformTech13103("lifeformTech13103", PAGE_LIFEFORM),
    lifeformTech13104("lifeformTech13104", PAGE_LIFEFORM),
    lifeformTech13105("lifeformTech13105", PAGE_LIFEFORM),
    lifeformTech13106("lifeformTech13106", PAGE_LIFEFORM),
    lifeformTech13107("lifeformTech13107", PAGE_LIFEFORM),
    lifeformTech13108("lifeformTech13108", PAGE_LIFEFORM),
    lifeformTech13109("lifeformTech13109", PAGE_LIFEFORM),
    lifeformTech13110("lifeformTech13110", PAGE_LIFEFORM),
    lifeformTech13111("lifeformTech13111", PAGE_LIFEFORM),
    lifeformTech13112("lifeformTech13112", PAGE_LIFEFORM),
    //Kaelesh
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
