package org.enoch.snark.expression.function.eventfleet;

import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.model.types.FleetDirectionType;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventFleetFunctionsTest {

    @BeforeEach
    void clearEventFleets() {
        Navigator.getInstance().informAboutEventFleets(List.of());
    }

    @AfterEach
    void clearEventFleetsAfterTest() {
        Navigator.getInstance().informAboutEventFleets(List.of());
    }

    @Test
    void filtersByArrivalTimeHostilityCountAndMission() {
        LocalDateTime now = LocalDateTime.now();
        EventFleet matchingFleet = eventFleet(now.plusHours(2), true, 20, Mission.ATTACK);
        EventFleet earlyFleet = eventFleet(now.plusMinutes(30), true, 20, Mission.ATTACK);
        EventFleet smallFleet = eventFleet(now.plusHours(2), true, 8, Mission.ATTACK);
        EventFleet peacefulFleet = eventFleet(now.plusHours(2), false, 20, Mission.ATTACK);
        EventFleet otherMissionFleet = eventFleet(now.plusHours(2), true, 20, Mission.TRANSPORT);
        Navigator.getInstance().informAboutEventFleets(List.of(
                matchingFleet, earlyFleet, smallFleet, peacefulFleet, otherMissionFleet
        ));

        List<EventFleet> result = EventFleetFunctions.filter(Map.of(
                "arrivalTimeLessThan", "3H",
                "arrivalTimeGreaterThan", "1H",
                "isHostile", true,
                "countGreaterThan", 10,
                "countLessThan", 25,
                "mission", "ATTACK"
        ));

        assertEquals(List.of(matchingFleet), result);
    }

    @Test
    void rejectsUnknownFiltersAndInvalidMissionNames() {
        assertThrows(IllegalArgumentException.class, () ->
                EventFleetFunctions.filter(Map.of("arrivalTimeGretherThan", "1H"))
        );
        assertThrows(IllegalArgumentException.class, () ->
                EventFleetFunctions.filter(Map.of("mission", "NOT_A_MISSION"))
        );
    }

    @Test
    void returnsEndingPlanetFromEventFleet() {
        EventFleet fleet = eventFleet(LocalDateTime.now(), false, 1, Mission.TRANSPORT);

        assertEquals(fleet.getEndingPlanet(), EventFleetFunctions.endingPlanet(fleet));
        assertEquals(Planet.parse("p[1:1:2]"), EventFleetFunctions.endingPlanet(fleet));
    }

    private EventFleet eventFleet(LocalDateTime arrivalTime, boolean hostile, long count, Mission mission) {
        EventFleet fleet = new EventFleet();
        fleet.arrivalTime = arrivalTime;
        fleet.isHostile = hostile;
        fleet.detailsFleet = Long.toString(count);
        fleet.mission = mission;
        fleet.iconMovement = FleetDirectionType.THERE;
        fleet.coordsOrigin = "[1:1:1]";
        fleet.originFleet = ColonyType.PLANET;
        fleet.destCoords = "[1:1:2]";
        fleet.destFleet = ColonyType.PLANET;
        return fleet;
    }
}
