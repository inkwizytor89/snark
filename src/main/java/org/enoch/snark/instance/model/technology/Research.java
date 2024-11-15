package org.enoch.snark.instance.model.technology;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.enoch.snark.gi.types.UrlComponent;

import static org.enoch.snark.gi.types.UrlComponent.RESEARCH;

@Getter
@AllArgsConstructor
public enum Research implements Technology {

    energyTechnology(113L),
    laserTechnology(120L),
    ionTechnology(121L),
    hyperspaceTechnology(114L),
    plasmaTechnology(122L),

    combustionDriveTechnology(115L),
    impulseDriveTechnology(117L),
    hyperspaceDriveTechnology(118L),

    espionageTechnology(106L),
    computerTechnology(108L),
    astrophysicsTechnology(124L),
    researchNetworkTechnology(123L),
    gravitonTechnology(199L),

    weaponsTechnology(109L),
    shieldingTechnology(110L),
    armorTechnology(111L);

    private final Long id;

    @Override
    public UrlComponent getPage() {
        return RESEARCH;
    }
}
