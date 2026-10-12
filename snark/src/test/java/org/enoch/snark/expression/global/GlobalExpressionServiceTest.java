package org.enoch.snark.expression.global;

import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.action.condition.AttackNotExpiredCondition;
import org.enoch.snark.instance.model.action.condition.ExpressionCondition;
import org.enoch.snark.instance.model.action.condition.FleetExpiredCondition;
import org.enoch.snark.instance.model.action.condition.FleetSlotCondition;
import org.enoch.snark.instance.model.action.condition.IsModifiedAfterCondition;
import org.enoch.snark.instance.model.action.condition.NoMissionsCondition;
import org.enoch.snark.instance.model.action.condition.ResourceCondition;
import org.enoch.snark.instance.model.action.condition.ResourceCountCondition;
import org.enoch.snark.instance.model.action.condition.ShipsCondition;
import org.enoch.snark.instance.model.action.condition.SpyNotExpiredCondition;
import org.enoch.snark.instance.model.technology.Ship;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalExpressionService Tests")
class GlobalExpressionServiceTest {

    private GlobalExpressionService expressionService;

    @BeforeEach
    void setUp() {
        expressionService = new GlobalExpressionService();
        expressionService.invalidateAll();
    }

    @Nested
    @DisplayName("Condition Function Tests")
    class ConditionFunctionsTests {

        @Test
        @DisplayName("Should evaluate resourceCount condition")
        void testResourceCountCondition() {
            AbstractCondition result = expressionService.evaluate(
                    "#hasResourcesCount({source: 'source', count: '3m'})",
                    Map.of(),
                    AbstractCondition.class,
                    Duration.ofMinutes(10),
                    "conditions"
            );

            assertInstanceOf(ResourceCountCondition.class, result);
            ResourceCountCondition condition = (ResourceCountCondition) result;
            assertEquals("3m", condition.getResourcesCount());
            assertEquals(Planet.parse("source"), condition.getPlanet());
        }

        @Test
        @DisplayName("Should evaluate resource condition with leave amount")
        void testResourceCondition() {
            AbstractCondition result = expressionService.evaluate(
                    "#hasResources({planet: 'target', have: 'm100 c50', leave: 'm10'})",
                    Map.of(),
                    AbstractCondition.class,
                    Duration.ofMinutes(10),
                    "conditions"
            );

            assertInstanceOf(ResourceCondition.class, result);
            ResourceCondition condition = (ResourceCondition) result;
            assertEquals(Planet.parse("target"), condition.getPlanet());
            assertEquals(100L, condition.getResources().metal);
            assertEquals(10L, condition.getLeaveResources().metal);
        }

        @Test
        @DisplayName("Should evaluate ships condition without leave")
        void testShipsCondition() {
            AbstractCondition result = expressionService.evaluate(
                    "#areShips({planet: 'source', need: 'transporterSmall:5,battleship:3'})",
                    Map.of(),
                    AbstractCondition.class,
                    Duration.ofMinutes(10),
                    "conditions"
            );

            assertInstanceOf(ShipsCondition.class, result);
            ShipsCondition condition = (ShipsCondition) result;
            assertEquals(5L, condition.getShipsMap().get(Ship.transporterSmall));
            assertSame(org.enoch.snark.instance.model.to.ShipsMap.NO_SHIPS, condition.getLeave());
        }

        @Test
        @DisplayName("Should evaluate fleetExpired condition")
        void testFleetExpiredCondition() {
            AbstractCondition result = expressionService.evaluate(
                    "#isFleetExpired({source: 'source', missions: 'TRANSPORT|ATTACK', timeout: '30M'})",
                    Map.of(),
                    AbstractCondition.class,
                    Duration.ofMinutes(10),
                    "conditions"
            );

            assertInstanceOf(FleetExpiredCondition.class, result);
            FleetExpiredCondition condition = (FleetExpiredCondition) result;
            assertEquals(Planet.parse("source"), condition.getSource());
            assertTrue(condition.getBlockingMissions().contains(Mission.TRANSPORT));
            assertTrue(condition.getBlockingMissions().contains(Mission.ATTACK));
            assertEquals(1800L, condition.getDuration().getSeconds());
        }

        @Test
        @DisplayName("Should evaluate expr condition")
        void testExprCondition() {
            AbstractCondition result = expressionService.evaluate(
                    "#isExpressionTrue({term: 'fuel_level', op: 'eq', value: 'ready'})",
                    Map.of(),
                    AbstractCondition.class,
                    Duration.ofMinutes(10),
                    "conditions"
            );

            assertInstanceOf(ExpressionCondition.class, result);
            ExpressionCondition condition = (ExpressionCondition) result;
            assertEquals("fuel_level", condition.getTerm());
            assertEquals("eq", condition.getOperator());
            assertEquals("ready", condition.getValue());
        }

        @Test
        @DisplayName("Should fail for tech condition without concrete implementation")
        void testTechCondition() {
            assertThrows(IllegalStateException.class, () ->
                    expressionService.evaluate(
                            "#hasTechnology({source: 'source', tech: 'IMPULSE', level: '5'})",
                            Map.of(),
                            AbstractCondition.class,
                            Duration.ofMinutes(10),
                            "conditions"
                    )
            );
        }

        @Test
        @DisplayName("Should evaluate noMissions condition")
        void testNoMissionsCondition() {
            AbstractCondition result = expressionService.evaluate(
                    "#hasNoMissions({planet: 'target'})",
                    Map.of(),
                    AbstractCondition.class,
                    Duration.ofMinutes(10),
                    "conditions"
            );

            assertInstanceOf(NoMissionsCondition.class, result);
            NoMissionsCondition condition = (NoMissionsCondition) result;
            assertEquals(Planet.parse("target"), condition.getSource());
            assertFalse(condition.getBlockingMissions().isEmpty());
        }

        @Test
        @DisplayName("Should evaluate fleetSlot condition")
        void testFleetSlotCondition() {
            AbstractCondition result = expressionService.evaluate(
                    "#hasFleetSlots({required: '5'})",
                    Map.of(),
                    AbstractCondition.class,
                    Duration.ofMinutes(10),
                    "conditions"
            );

            assertInstanceOf(FleetSlotCondition.class, result);
            assertEquals(5, ((FleetSlotCondition) result).getFreeSlots());
        }

        @Test
        @DisplayName("Should evaluate attackNotExpired condition")
        void testAttackNotExpiredCondition() {
            AbstractCondition result = expressionService.evaluate(
                    "#isAttackNotExpired({target: 'target', timeout: '1H'})",
                    Map.of(),
                    AbstractCondition.class,
                    Duration.ofMinutes(10),
                    "conditions"
            );

            assertInstanceOf(AttackNotExpiredCondition.class, result);
            AttackNotExpiredCondition condition = (AttackNotExpiredCondition) result;
            assertEquals(Planet.parse("target"), condition.getTarget());
            assertEquals(3600L, condition.getSeconds());
            assertFalse(condition.getIs());
        }

        @Test
        @DisplayName("Should evaluate spyNotExpired condition")
        void testSpyNotExpiredCondition() {
            AbstractCondition result = expressionService.evaluate(
                    "#isSpyNotExpired({target: 'target'})",
                    Map.of(),
                    AbstractCondition.class,
                    Duration.ofMinutes(10),
                    "conditions"
            );

            assertInstanceOf(SpyNotExpiredCondition.class, result);
            SpyNotExpiredCondition condition = (SpyNotExpiredCondition) result;
            assertEquals(Planet.parse("target"), condition.getTarget());
            assertEquals(3600L, condition.getSeconds());
            assertFalse(condition.getIs());
        }

        @Test
        @DisplayName("Should evaluate modifiedAfter condition")
        void testModifiedAfterCondition() {
            AbstractCondition result = expressionService.evaluate(
                    "#isModifiedAfter({first: 'scanFinished', second: 'fleetSent'})",
                    Map.of(),
                    AbstractCondition.class,
                    Duration.ofMinutes(10),
                    "conditions"
            );

            assertInstanceOf(IsModifiedAfterCondition.class, result);
            IsModifiedAfterCondition condition = (IsModifiedAfterCondition) result;
            assertEquals("scanFinished", condition.getFirstTerm());
            assertEquals("fleetSent", condition.getSecondTerm());
        }
    }

    @Nested
    @DisplayName("Caching Tests")
    class CachingTests {

        @Test
        @DisplayName("Should cache identical expressions")
        void testCaching() {
            String expr = "#hasResourcesCount({source: 'source', count: '3m'})";

            assertEquals(0, expressionService.getCacheSize());

            AbstractCondition result1 = expressionService.evaluate(
                    expr,
                    Map.of(),
                    AbstractCondition.class,
                    Duration.ofMinutes(10),
                    "conditions"
            );

            assertEquals(1, expressionService.getCacheSize());

            AbstractCondition result2 = expressionService.evaluate(
                    expr,
                    Map.of(),
                    AbstractCondition.class,
                    Duration.ofMinutes(10),
                    "conditions"
            );

            assertEquals(1, expressionService.getCacheSize());
            assertSame(result1, result2);
        }

        @Test
        @DisplayName("Should not cache different expressions")
        void testNoCacheForDifferentExpressions() {
            expressionService.evaluate("#hasResourcesCount({source: 'source', count: '3m'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10), "conditions");
            expressionService.evaluate("#hasResourcesCount({source: 'source', count: '5m'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10), "conditions");

            assertEquals(2, expressionService.getCacheSize());
        }

        @Test
        @DisplayName("Should handle different contexts")
        void testCachingWithContext() {
            String expr = "#hasResourcesCount({source: 'source', count: '3m'})";

            expressionService.evaluate(expr, Map.of("param", "value1"), AbstractCondition.class, Duration.ofMinutes(10), "conditions");
            expressionService.evaluate(expr, Map.of("param", "value2"), AbstractCondition.class, Duration.ofMinutes(10), "conditions");

            assertEquals(2, expressionService.getCacheSize());
        }
    }

    @Nested
    @DisplayName("Cache Invalidation Tests")
    class InvalidationTests {

        @Test
        @DisplayName("Should invalidate by domain")
        void testInvalidateByDomain() {
            expressionService.evaluate("#hasResourcesCount({source: 'source', count: '3m'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10), "conditions");
            expressionService.evaluate("#areShips({planet: 'source', need: 'transporterSmall:5'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10), "coordinates");

            assertEquals(2, expressionService.getCacheSize());

            expressionService.invalidateByDomain("conditions");
            assertEquals(1, expressionService.getCacheSize());
        }

        @Test
        @DisplayName("Should invalidate all")
        void testInvalidateAll() {
            expressionService.evaluate("#hasResourcesCount({source: 'source', count: '3m'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10), "conditions");
            expressionService.evaluate("#areShips({planet: 'source', need: 'transporterSmall:5'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10), "conditions");

            assertEquals(2, expressionService.getCacheSize());

            expressionService.invalidateAll();
            assertEquals(0, expressionService.getCacheSize());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw on null expression")
        void testNullExpression() {
            assertThrows(IllegalArgumentException.class, () ->
                    expressionService.evaluate(null, Map.of(), AbstractCondition.class, Duration.ofMinutes(10), "conditions")
            );
        }

        @Test
        @DisplayName("Should throw on empty expression")
        void testEmptyExpression() {
            assertThrows(IllegalArgumentException.class, () ->
                    expressionService.evaluate("", Map.of(), AbstractCondition.class, Duration.ofMinutes(10), "conditions")
            );
        }

        @Test
        @DisplayName("Should throw on invalid SpEL expression")
        void testInvalidExpression() {
            assertThrows(IllegalStateException.class, () ->
                    expressionService.evaluate("#nonExistentFunction({param: 'value'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10), "conditions")
            );
        }

        @Test
        @DisplayName("Should throw on missing required parameter")
        void testMissingRequiredParameter() {
            assertThrows(IllegalStateException.class, () ->
                    expressionService.evaluate("#hasResourcesCount({source: 'source'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10), "conditions")
            );
        }
    }

    @Nested
    @DisplayName("NoCached Evaluation Tests")
    class NoCachedTests {

        @Test
        @DisplayName("Should evaluate without caching")
        void testNoCachedEvaluation() {
            AbstractCondition result = expressionService.evaluateNoCached(
                    "#hasResourcesCount({source: 'source', count: '3m'})",
                    Map.of(),
                    AbstractCondition.class
            );

            assertInstanceOf(ResourceCountCondition.class, result);
            assertEquals(0, expressionService.getCacheSize());
        }

        @Test
        @DisplayName("Should not interfere with cached evaluation")
        void testNoCachedDoesNotInterfere() {
            String expr = "#hasResourcesCount({source: 'source', count: '3m'})";

            expressionService.evaluate(expr, Map.of(), AbstractCondition.class, Duration.ofMinutes(10), "conditions");
            assertEquals(1, expressionService.getCacheSize());

            expressionService.evaluateNoCached(expr, Map.of(), AbstractCondition.class);
            assertEquals(1, expressionService.getCacheSize());
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should report cache stats")
        void testCacheStats() {
            expressionService.evaluate("#hasResourcesCount({source: 'source', count: '3m'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10), "conditions");
            expressionService.evaluate("#areShips({planet: 'source', need: 'transporterSmall:5'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10), "coordinates");

            String stats = expressionService.getCacheStats();
            assertNotNull(stats);
            assertTrue(stats.contains("2 entries"));
        }
    }
}
