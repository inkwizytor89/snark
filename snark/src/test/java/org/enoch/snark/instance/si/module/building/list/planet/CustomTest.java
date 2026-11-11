package org.enoch.snark.instance.si.module.building.list.planet;

import org.enoch.snark.instance.model.technology.Building;
import org.enoch.snark.instance.model.technology.Defense;
import org.enoch.snark.instance.model.technology.LFBuilding;
import org.enoch.snark.instance.model.technology.Research;
import org.enoch.snark.instance.model.technology.Ship;
import org.enoch.snark.instance.model.technology.Technology;
import org.enoch.snark.instance.si.module.building.BuildRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomTest {

    @Test
    void createsRequestsForEveryTechnologyType() {
        assertRequest("metalMine_2", Building.metalMine);
        assertRequest("energyTechnology_3", Research.energyTechnology);
        assertRequest("rocketLauncher_4", Defense.rocketLauncher);
        assertRequest("lifeformTech11101_5", LFBuilding.lifeformTech11101);
        assertRequest("fighterLight_6", Ship.fighterLight);
    }

    @Test
    void supportsQualifiedNamesForTechnologiesWithDuplicateNames() {
        assertRequest("Ship.solarSatellite_7", Ship.solarSatellite);
    }

    @Test
    void matchesTechnologyNamesAndQualifiedNamesWithoutCaseSensitivity() {
        assertRequest("METALMINE_2", Building.metalMine);
        assertRequest("eNeRgYtEcHnOlOgY_3", Research.energyTechnology);
        assertRequest("sHiP.sOlArSaTeLlItE_7", Ship.solarSatellite);
    }

    @Test
    void rejectsUnknownTechnologyNames() {
        assertThrows(IllegalArgumentException.class, () -> new Custom("notATechnology_1").create());
    }

    private void assertRequest(String definition, Technology expectedTechnology) {
        BuildRequest request = new Custom(definition).create().get(0);
        assertEquals(expectedTechnology, request.technology);
    }
}
