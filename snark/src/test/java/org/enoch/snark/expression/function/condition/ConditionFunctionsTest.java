package org.enoch.snark.expression.function.condition;

import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.action.condition.AreShipsCondition;
import org.enoch.snark.instance.model.action.condition.HasFleetSlotsCondition;
import org.enoch.snark.instance.model.action.condition.HasNoMissionsCondition;
import org.enoch.snark.instance.model.action.condition.HasResourcesCondition;
import org.enoch.snark.instance.model.action.condition.HasResourcesCountCondition;
import org.enoch.snark.instance.model.action.condition.IsAttackNotExpiredCondition;
import org.enoch.snark.instance.model.action.condition.IsExpressionTrueCondition;
import org.enoch.snark.instance.model.action.condition.IsFleetExpiredCondition;
import org.enoch.snark.instance.model.action.condition.IsModifiedAfterCondition;
import org.enoch.snark.instance.model.action.condition.IsSpyNotExpiredCondition;
import org.enoch.snark.instance.model.technology.Ship;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConditionFunctions Tests")
class ConditionFunctionsTest {

    @Test
    @DisplayName("Should create resourceCount condition")
    void testResourceCountCondition() {
        AbstractCondition result = ConditionFunctions.hasResourcesCount(Map.of("source", "source", "count", "3m"));

        assertInstanceOf(HasResourcesCountCondition.class, result);
        HasResourcesCountCondition condition = (HasResourcesCountCondition) result;
        assertEquals("3m", condition.getResourcesCount());
        assertEquals(Planet.parse("source"), condition.getPlanet());
    }

    @Test
    @DisplayName("Should create resource condition with optional leave")
    void testResourceCondition() {
        AbstractCondition result = ConditionFunctions.hasResources(
                Map.of("planet", "target", "have", "m100 c50", "leave", "m10")
        );

        assertInstanceOf(HasResourcesCondition.class, result);
        HasResourcesCondition condition = (HasResourcesCondition) result;
        assertEquals(Planet.parse("target"), condition.getPlanet());
        assertEquals(100L, condition.getResources().metal);
        assertEquals(50L, condition.getResources().crystal);
        assertEquals(10L, condition.getLeaveResources().metal);
    }

    @Test
    @DisplayName("Should create resource condition without leave")
    void testResourceConditionWithoutLeave() {
        AbstractCondition result = ConditionFunctions.hasResources(
                Map.of("planet", "source", "have", "c50")
        );

        assertInstanceOf(HasResourcesCondition.class, result);
        HasResourcesCondition condition = (HasResourcesCondition) result;
        assertEquals(0L, condition.getLeaveResources().count());
    }

    @Test
    @DisplayName("Should create ships condition")
    void testShipsCondition() {
        AbstractCondition result = ConditionFunctions.areShips(
                Map.of("planet", "source", "need", "transporterSmall:5,battleship:3", "leave", "transporterSmall:2")
        );

        assertInstanceOf(AreShipsCondition.class, result);
        AreShipsCondition condition = (AreShipsCondition) result;
        assertEquals(Planet.parse("source"), condition.getPlanet());
        assertEquals(5L, condition.getShipsMap().get(Ship.transporterSmall));
        assertEquals(3L, condition.getShipsMap().get(Ship.battleship));
        assertEquals(2L, condition.getLeave().get(Ship.transporterSmall));
    }

    @Test
    @DisplayName("Should create ships condition without leave")
    void testShipsConditionWithoutLeave() {
        AbstractCondition result = ConditionFunctions.areShips(
                Map.of("planet", "target", "need", "battleship:10")
        );

        assertInstanceOf(AreShipsCondition.class, result);
        AreShipsCondition condition = (AreShipsCondition) result;
        assertSame(org.enoch.snark.instance.model.to.ShipsMap.NO_SHIPS, condition.getLeave());
    }

    @Test
    @DisplayName("Should create fleetExpired condition")
    void testFleetExpiredCondition() {
        AbstractCondition result = ConditionFunctions.isFleetExpired(
                Map.of("source", "source", "missions", "TRANSPORT|ATTACK", "timeout", "30M")
        );

        assertInstanceOf(IsFleetExpiredCondition.class, result);
        IsFleetExpiredCondition condition = (IsFleetExpiredCondition) result;
        assertEquals(Planet.parse("source"), condition.getSource());
        assertTrue(condition.getBlockingMissions().contains(Mission.TRANSPORT));
        assertTrue(condition.getBlockingMissions().contains(Mission.ATTACK));
        assertEquals(1800L, condition.getDuration().getSeconds());
    }

    @Test
    @DisplayName("Should use default timeout for fleetExpired")
    void testFleetExpiredConditionDefaultTimeout() {
        IsFleetExpiredCondition condition = (IsFleetExpiredCondition) ConditionFunctions.isFleetExpired(
                Map.of("source", "source", "missions", "TRANSPORT")
        );

        assertEquals(3600L, condition.getDuration().getSeconds());
    }

    @Test
    @DisplayName("Should create expr condition")
    void testExprCondition() {
        AbstractCondition result = ConditionFunctions.isExpressionTrue(
                Map.of("term", "fuel_level", "op", "eq", "value", "ready")
        );

        assertInstanceOf(IsExpressionTrueCondition.class, result);
        IsExpressionTrueCondition condition = (IsExpressionTrueCondition) result;
        assertEquals("fuel_level", condition.getTerm());
        assertEquals("eq", condition.getOperator());
        assertEquals("ready", condition.getValue());
    }

    @Test
    @DisplayName("Should reject tech condition until concrete implementation exists")
    void testTechCondition() {
        assertThrows(UnsupportedOperationException.class, () ->
                ConditionFunctions.hasTechnology(Map.of("source", "source", "tech", "IMPULSE", "level", "5"))
        );
    }

    @Test
    @DisplayName("Should create noMissions condition")
    void testNoMissionsCondition() {
        AbstractCondition result = ConditionFunctions.hasNoMissions(Map.of("planet", "target"));

        assertInstanceOf(HasNoMissionsCondition.class, result);
        HasNoMissionsCondition condition = (HasNoMissionsCondition) result;
        assertEquals(Planet.parse("target"), condition.getSource());
        assertFalse(condition.getBlockingMissions().isEmpty());
        assertEquals(3600L, condition.getDuration().getSeconds());
    }

    @Test
    @DisplayName("Should create fleetSlot condition")
    void testFleetSlotCondition() {
        AbstractCondition result = ConditionFunctions.hasFleetSlots(Map.of("required", "5"));

        assertInstanceOf(HasFleetSlotsCondition.class, result);
        assertEquals(5, ((HasFleetSlotsCondition) result).getFreeSlots());
    }

    @Test
    @DisplayName("Should create attackNotExpired condition")
    void testAttackNotExpiredCondition() {
        AbstractCondition result = ConditionFunctions.isAttackNotExpired(
                Map.of("target", "target", "timeout", "1H")
        );

        assertInstanceOf(IsAttackNotExpiredCondition.class, result);
        IsAttackNotExpiredCondition condition = (IsAttackNotExpiredCondition) result;
        assertEquals(Planet.parse("target"), condition.getTarget());
        assertEquals(3600L, condition.getSeconds());
        assertFalse(condition.getIs());
    }

    @Test
    @DisplayName("Should use default timeout for attackNotExpired")
    void testAttackNotExpiredDefaultTimeout() {
        IsAttackNotExpiredCondition condition = (IsAttackNotExpiredCondition) ConditionFunctions.isAttackNotExpired(
                Map.of("target", "target")
        );

        assertEquals(3600L, condition.getSeconds());
        assertFalse(condition.getIs());
    }

    @Test
    @DisplayName("Should create spyNotExpired condition")
    void testSpyNotExpiredCondition() {
        AbstractCondition result = ConditionFunctions.isSpyNotExpired(
                Map.of("target", "target", "timeout", "30M")
        );

        assertInstanceOf(IsSpyNotExpiredCondition.class, result);
        IsSpyNotExpiredCondition condition = (IsSpyNotExpiredCondition) result;
        assertEquals(Planet.parse("target"), condition.getTarget());
        assertEquals(1800L, condition.getSeconds());
        assertFalse(condition.getIs());
    }

    @Test
    @DisplayName("Should use default timeout for spyNotExpired")
    void testSpyNotExpiredDefaultTimeout() {
        IsSpyNotExpiredCondition condition = (IsSpyNotExpiredCondition) ConditionFunctions.isSpyNotExpired(
                Map.of("target", "target")
        );

        assertEquals(3600L, condition.getSeconds());
        assertFalse(condition.getIs());
    }

    @Test
    @DisplayName("Should create modifiedAfter condition")
    void testModifiedAfterCondition() {
        AbstractCondition result = ConditionFunctions.isModifiedAfter(
                Map.of("first", "scanFinished", "second", "fleetSent")
        );

        assertInstanceOf(IsModifiedAfterCondition.class, result);
        IsModifiedAfterCondition condition = (IsModifiedAfterCondition) result;
        assertEquals("scanFinished", condition.getFirstTerm());
        assertEquals("fleetSent", condition.getSecondTerm());
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw on missing required parameter")
        void testMissingRequiredParameter() {
            assertThrows(IllegalArgumentException.class, () ->
                    ConditionFunctions.hasResourcesCount(Map.of("source", "source"))
            );
        }

        @Test
        @DisplayName("Should throw on invalid planet format")
        void testInvalidPlanetFormat() {
            assertThrows(IllegalStateException.class, () ->
                    ConditionFunctions.hasResourcesCount(Map.of("source", "invalid_planet", "count", "3m"))
            );
        }

        @Test
        @DisplayName("Should throw on invalid duration format")
        void testInvalidDurationFormat() {
            assertThrows(IllegalArgumentException.class, () ->
                    ConditionFunctions.isFleetExpired(
                            Map.of("source", "source", "missions", "TRANSPORT", "timeout", "invalid")
                    )
            );
        }
    }
}
