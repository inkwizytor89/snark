package org.enoch.snark.expression.global;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ConditionFunctions.
 */
@DisplayName("ConditionFunctions Tests")
class ConditionFunctionsTest {

    @Test
    @DisplayName("Should create resourceCount condition")
    void testResourceCountCondition() {
        ConditionDto result = ConditionFunctions.resourceCount(
            Map.of("source", "source", "count", "3m")
        );

        assertNotNull(result);
        assertEquals("RESOURCE_COUNT", result.getType());
        assertEquals("source", result.getParameter("source"));
        assertEquals("3m", result.getParameter("count"));
    }

    @Test
    @DisplayName("Should create resource condition with optional leave")
    void testResourceCondition() {
        ConditionDto result = ConditionFunctions.resource(
            Map.of(
                "planet", "target",
                "have", "metal:100 crystal:50",
                "leave", "metal:10"
            )
        );

        assertNotNull(result);
        assertEquals("RESOURCE", result.getType());
        assertEquals("target", result.getParameter("planet"));
        assertNotNull(result.getParameter("have"));
        assertNotNull(result.getParameter("leave"));
    }

    @Test
    @DisplayName("Should create resource condition without leave")
    void testResourceConditionWithoutLeave() {
        ConditionDto result = ConditionFunctions.resource(
            Map.of(
                "planet", "source",
                "have", "crystal:50"
            )
        );

        assertNotNull(result);
        assertEquals("RESOURCE", result.getType());
        Map<String, String> leaveResources = (Map<String, String>) result.getParameter("leave");
        assertTrue(leaveResources.isEmpty());
    }

    @Test
    @DisplayName("Should create ships condition")
    void testShipsCondition() {
        ConditionDto result = ConditionFunctions.ships(
            Map.of(
                "planet", "source",
                "need", "sg:5 bc:3",
                "leave", "sg:2"
            )
        );

        assertNotNull(result);
        assertEquals("SHIPS", result.getType());
        assertEquals("source", result.getParameter("planet"));
        assertNotNull(result.getParameter("need"));
        assertNotNull(result.getParameter("leave"));
    }

    @Test
    @DisplayName("Should create ships condition without leave")
    void testShipsConditionWithoutLeave() {
        ConditionDto result = ConditionFunctions.ships(
            Map.of(
                "planet", "target",
                "need", "bc:10"
            )
        );

        assertNotNull(result);
        assertEquals("SHIPS", result.getType());
        Map<String, Integer> leaveShips = (Map<String, Integer>) result.getParameter("leave");
        assertTrue(leaveShips.isEmpty());
    }

    @Test
    @DisplayName("Should create fleetExpired condition")
    void testFleetExpiredCondition() {
        ConditionDto result = ConditionFunctions.fleetExpired(
            Map.of(
                "source", "source",
                "missions", "TRANSPORT|ATTACK",
                "timeout", "30M"
            )
        );

        assertNotNull(result);
        assertEquals("FLEET_EXPIRED", result.getType());
        assertEquals("source", result.getParameter("source"));
        assertEquals("TRANSPORT|ATTACK", result.getParameter("missions"));
        assertEquals(30 * 60 * 1000L, result.getParameter("timeoutMs"));
    }

    @Test
    @DisplayName("Should use default timeout for fleetExpired")
    void testFleetExpiredConditionDefaultTimeout() {
        ConditionDto result = ConditionFunctions.fleetExpired(
            Map.of(
                "source", "source",
                "missions", "TRANSPORT"
            )
        );

        assertNotNull(result);
        assertEquals(60 * 60 * 1000L, result.getParameter("timeoutMs")); // 1H default
    }

    @Test
    @DisplayName("Should create expr condition")
    void testExprCondition() {
        ConditionDto result = ConditionFunctions.expr(
            Map.of(
                "term", "fuel_level",
                "op", "eq",
                "value", "ready"
            )
        );

        assertNotNull(result);
        assertEquals("EXPR", result.getType());
        assertEquals("fuel_level", result.getParameter("term"));
        assertEquals("eq", result.getParameter("operator"));
        assertEquals("ready", result.getParameter("value"));
    }

    @Test
    @DisplayName("Should create tech condition")
    void testTechCondition() {
        ConditionDto result = ConditionFunctions.tech(
            Map.of(
                "source", "source",
                "tech", "IMPULSE",
                "level", "5"
            )
        );

        assertNotNull(result);
        assertEquals("TECH", result.getType());
        assertEquals("source", result.getParameter("source"));
        assertEquals("IMPULSE", result.getParameter("technology"));
        assertEquals(5L, result.getParameter("level"));
    }

    @Test
    @DisplayName("Should create noMissions condition")
    void testNoMissionsCondition() {
        ConditionDto result = ConditionFunctions.noMissions(
            Map.of("planet", "target")
        );

        assertNotNull(result);
        assertEquals("NO_MISSIONS", result.getType());
        assertEquals("target", result.getParameter("planet"));
    }

    @Test
    @DisplayName("Should create fleetSlot condition")
    void testFleetSlotCondition() {
        ConditionDto result = ConditionFunctions.fleetSlot(
            Map.of(
                "planet", "source",
                "required", "5"
            )
        );

        assertNotNull(result);
        assertEquals("FLEET_SLOT", result.getType());
        assertEquals("source", result.getParameter("planet"));
        assertEquals(5, result.getParameter("required"));
    }

    @Test
    @DisplayName("Should create attackNotExpired condition")
    void testAttackNotExpiredCondition() {
        ConditionDto result = ConditionFunctions.attackNotExpired(
            Map.of(
                "target", "target",
                "timeout", "1H"
            )
        );

        assertNotNull(result);
        assertEquals("ATTACK_NOT_EXPIRED", result.getType());
        assertEquals("target", result.getParameter("target"));
        assertEquals(60 * 60 * 1000L, result.getParameter("timeoutMs"));
    }

    @Test
    @DisplayName("Should use default timeout for attackNotExpired")
    void testAttackNotExpiredDefaultTimeout() {
        ConditionDto result = ConditionFunctions.attackNotExpired(
            Map.of("target", "target")
        );

        assertNotNull(result);
        assertEquals(60 * 60 * 1000L, result.getParameter("timeoutMs")); // 1H default
    }

    @Test
    @DisplayName("Should create spyNotExpired condition")
    void testSpyNotExpiredCondition() {
        ConditionDto result = ConditionFunctions.spyNotExpired(
            Map.of(
                "target", "target",
                "timeout", "30M"
            )
        );

        assertNotNull(result);
        assertEquals("SPY_NOT_EXPIRED", result.getType());
        assertEquals("target", result.getParameter("target"));
        assertEquals(30 * 60 * 1000L, result.getParameter("timeoutMs"));
    }

    @Test
    @DisplayName("Should use default timeout for spyNotExpired")
    void testSpyNotExpiredDefaultTimeout() {
        ConditionDto result = ConditionFunctions.spyNotExpired(
            Map.of("target", "target")
        );

        assertNotNull(result);
        assertEquals(60 * 60 * 1000L, result.getParameter("timeoutMs")); // 1H default
    }

    @Test
    @DisplayName("Should create modifiedAfter condition")
    void testModifiedAfterCondition() {
        ConditionDto result = ConditionFunctions.modifiedAfter(
            Map.of(
                "target", "source",
                "since", "10M"
            )
        );

        assertNotNull(result);
        assertEquals("IS_MODIFIED_AFTER", result.getType());
        assertEquals("source", result.getParameter("target"));
        assertEquals(10 * 60 * 1000L, result.getParameter("sinceMs"));
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw on missing required parameter")
        void testMissingRequiredParameter() {
            assertThrows(IllegalArgumentException.class, () ->
                ConditionFunctions.resourceCount(Map.of("source", "source"))
            );
        }

        @Test
        @DisplayName("Should throw on invalid planet format")
        void testInvalidPlanetFormat() {
            assertThrows(IllegalArgumentException.class, () ->
                ConditionFunctions.resourceCount(
                    Map.of("source", "invalid_planet", "count", "3m")
                )
            );
        }

        @Test
        @DisplayName("Should throw on invalid duration format")
        void testInvalidDurationFormat() {
            assertThrows(IllegalArgumentException.class, () ->
                ConditionFunctions.fleetExpired(
                    Map.of(
                        "source", "source",
                        "missions", "TRANSPORT",
                        "timeout", "invalid"
                    )
                )
            );
        }

        @Test
        @DisplayName("Should throw on invalid technology level")
        void testInvalidTechLevel() {
            assertThrows(NumberFormatException.class, () ->
                ConditionFunctions.tech(
                    Map.of(
                        "source", "source",
                        "tech", "IMPULSE",
                        "level", "not_a_number"
                    )
                )
            );
        }
    }
}
