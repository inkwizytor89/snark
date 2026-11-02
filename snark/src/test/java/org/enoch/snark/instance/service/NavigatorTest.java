package org.enoch.snark.instance.service;

import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.model.types.FleetDirectionType;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NavigatorTest {

    @AfterEach
    void clearEventFleets() {
        Navigator.getInstance().informAboutEventFleets(List.of());
    }

    @Test
    void missionSummaryIncludesReturningFleetsAndStationedFleetsGoingThere() {
        Navigator.getInstance().informAboutEventFleets(List.of(
                eventFleet(Mission.ATTACK, FleetDirectionType.BACK),
                eventFleet(Mission.STATIONED, FleetDirectionType.THERE),
                eventFleet(Mission.STATIONED, FleetDirectionType.BACK),
                eventFleet(Mission.ATTACK, FleetDirectionType.THERE),
                eventFleet(Mission.TRANSPORT, FleetDirectionType.THERE)
        ));

        assertEquals(Map.of("ATTACK", 1L, "STATIONED", 2L), Navigator.getInstance().getStatus().getMissions());
    }

    private EventFleet eventFleet(Mission mission, FleetDirectionType direction) {
        EventFleet fleet = new EventFleet();
        fleet.mission = mission;
        fleet.iconMovement = direction;
        fleet.coordsOrigin = "[1:1:1]";
        fleet.originFleet = ColonyType.PLANET;
        fleet.destCoords = "[1:1:2]";
        fleet.destFleet = ColonyType.PLANET;
        fleet.detailsFleet = "0";
        return fleet;
    }
}
