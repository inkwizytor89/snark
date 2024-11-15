package org.enoch.snark.instance.model.technology;

import lombok.Getter;
import org.enoch.snark.gi.types.UrlComponent;

import java.util.Arrays;
import java.util.List;

import static org.enoch.snark.gi.types.UrlComponent.*;

@Getter
public enum LFBuilding implements Technology {

    // Humans
    lifeformTech11101(11101L),
    lifeformTech11102(11102L),
    lifeformTech11103(11103L),
    lifeformTech11104(11104L),
    lifeformTech11105(11105L),
    lifeformTech11106(11106L),
    lifeformTech11107(11107L),
    lifeformTech11108(11108L),
    lifeformTech11109(11109L),
    lifeformTech11110(11110L),
    lifeformTech11111(11111L),
    lifeformTech11112(11112L),

    // Rocktal
    lifeformTech12101(12101L),
    lifeformTech12102(12102L),
    lifeformTech12103(12103L),
    lifeformTech12104(12104L),
    lifeformTech12105(12105L),
    lifeformTech12106(12106L),
    lifeformTech12107(12107L),
    lifeformTech12108(12108L),
    lifeformTech12109(12109L),
    lifeformTech12110(12110L),
    lifeformTech12111(12111L),
    lifeformTech12112(12112L),

    // Mecha
    lifeformTech13101(13101L),
    lifeformTech13102(13102L),
    lifeformTech13103(13103L),
    lifeformTech13104(13104L),
    lifeformTech13105(13105L),
    lifeformTech13106(13106L),
    lifeformTech13107(13107L),
    lifeformTech13108(13108L),
    lifeformTech13109(13109L),
    lifeformTech13110(13110L),
    lifeformTech13111(13111L),
    lifeformTech13112(13112L),

    //Kaelesh
    lifeformTech14101(14101L),
    lifeformTech14102(14102L),
    lifeformTech14103(14103L),
    lifeformTech14104(14104L),
    lifeformTech14105(14105L),
    lifeformTech14106(14106L),
    lifeformTech14107(14107L),
    lifeformTech14108(14108L),
    lifeformTech14109(14109L),
    lifeformTech14110(14110L),
    lifeformTech14111(14111L),
    lifeformTech14112(14112L);

    private final Long id;

    LFBuilding(Long id) {
        this.id = id;
    }

    public static List<LFBuilding> baseBuildings() {
        return Arrays.stream(LFBuilding.values())
                .filter(buildingEnum -> !buildingEnum.isLifeform())
                .toList();
    }

    public boolean isLifeform() {
        return name().contains("lifeform");
    }

    public UrlComponent getPage() {
        return LFBUILDINGS;
    }
}
