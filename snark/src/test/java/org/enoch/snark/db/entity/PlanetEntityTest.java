package org.enoch.snark.db.entity;

import org.enoch.snark.instance.model.technology.Defense;
import org.enoch.snark.instance.model.technology.LFBuilding;
import org.enoch.snark.instance.model.technology.Ship;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlanetEntityTest {

    @Test
    void returnsLevelsForEveryShip() {
        ColonyEntity planet = new ColonyEntity();
        long level = 1;
        for (Ship ship : Ship.values()) {
            planet.put(ship, level);
            assertEquals(level, planet.getBuildingLevel(ship), ship.name());
            level++;
        }
    }

    @Test
    void returnsLevelsForEveryDefense() {
        ColonyEntity planet = new ColonyEntity();
        long level = 1;
        for (Defense defense : Defense.values()) {
            planet.put(defense, level);
            assertEquals(level, planet.getBuildingLevel(defense), defense.name());
            level++;
        }
    }

    @Test
    void returnsLevelsForLifeformBuildings() {
        ColonyEntity planet = new ColonyEntity();
        planet.lifeformTech11101 = 3L;
        planet.lifeformTech14112 = 7L;

        assertEquals(3L, planet.getBuildingLevel(LFBuilding.lifeformTech11101));
        assertEquals(7L, planet.getBuildingLevel(LFBuilding.lifeformTech14112));
    }
}
