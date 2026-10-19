package org.enoch.snark.expression.global;

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
                    Duration.ofMinutes(10)
            );

            assertInstanceOf(HasResourcesCountCondition.class, result);
            HasResourcesCountCondition condition = (HasResourcesCountCondition) result;
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
                    Duration.ofMinutes(10)
            );

            assertInstanceOf(HasResourcesCondition.class, result);
            HasResourcesCondition condition = (HasResourcesCondition) result;
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
                    Duration.ofMinutes(10)
            );

            assertInstanceOf(AreShipsCondition.class, result);
            AreShipsCondition condition = (AreShipsCondition) result;
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
                    Duration.ofMinutes(10)
            );

            assertInstanceOf(IsFleetExpiredCondition.class, result);
            IsFleetExpiredCondition condition = (IsFleetExpiredCondition) result;
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
                    Duration.ofMinutes(10)
            );

            assertInstanceOf(IsExpressionTrueCondition.class, result);
            IsExpressionTrueCondition condition = (IsExpressionTrueCondition) result;
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
                            Duration.ofMinutes(10)
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
                    Duration.ofMinutes(10)
            );

            assertInstanceOf(HasNoMissionsCondition.class, result);
            HasNoMissionsCondition condition = (HasNoMissionsCondition) result;
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
                    Duration.ofMinutes(10)
            );

            assertInstanceOf(HasFleetSlotsCondition.class, result);
            assertEquals(5, ((HasFleetSlotsCondition) result).getFreeSlots());
        }

        @Test
        @DisplayName("Should evaluate attackNotExpired condition")
        void testAttackNotExpiredCondition() {
            AbstractCondition result = expressionService.evaluate(
                    "#isAttackNotExpired({target: 'target', timeout: '1H'})",
                    Map.of(),
                    AbstractCondition.class,
                    Duration.ofMinutes(10)
            );

            assertInstanceOf(IsAttackNotExpiredCondition.class, result);
            IsAttackNotExpiredCondition condition = (IsAttackNotExpiredCondition) result;
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
                    Duration.ofMinutes(10)
            );

            assertInstanceOf(IsSpyNotExpiredCondition.class, result);
            IsSpyNotExpiredCondition condition = (IsSpyNotExpiredCondition) result;
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
                    Duration.ofMinutes(10)
            );

            assertInstanceOf(IsModifiedAfterCondition.class, result);
            IsModifiedAfterCondition condition = (IsModifiedAfterCondition) result;
            assertEquals("scanFinished", condition.getFirstTerm());
            assertEquals("fleetSent", condition.getSecondTerm());
        }

        @Test
        @DisplayName("Should evaluate multiple conditions from single expression")
        void testConditionListExpression() {
            var conditions = expressionService.evaluateConditions(
                    "#hasResourcesCount({source: 'source', count: '3m'}) | #areShips({planet: 'source', need: 'transporterSmall:5'})",
                    Map.of()
            );

            assertEquals(2, conditions.size());
            assertInstanceOf(HasResourcesCountCondition.class, conditions.get(0));
            assertInstanceOf(AreShipsCondition.class, conditions.get(1));
        }

        @Test
        @DisplayName("Should not split on pipe inside missions value")
        void testConditionListWithPipeInsideValue() {
            var conditions = expressionService.evaluateConditions(
                    "#isFleetExpired({source: 'source', missions: 'TRANSPORT|ATTACK', timeout: '30M'}) | #hasFleetSlots({required: '1'})",
                    Map.of()
            );

            assertEquals(2, conditions.size());
            assertInstanceOf(IsFleetExpiredCondition.class, conditions.get(0));
            assertInstanceOf(HasFleetSlotsCondition.class, conditions.get(1));
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
                    Duration.ofMinutes(10)
            );

            assertEquals(1, expressionService.getCacheSize());

            AbstractCondition result2 = expressionService.evaluate(
                    expr,
                    Map.of(),
                    AbstractCondition.class,
                    Duration.ofMinutes(10)
            );

            assertEquals(1, expressionService.getCacheSize());
            assertSame(result1, result2);
        }

        @Test
        @DisplayName("Should not cache different expressions")
        void testNoCacheForDifferentExpressions() {
            expressionService.evaluate("#hasResourcesCount({source: 'source', count: '3m'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10));
            expressionService.evaluate("#hasResourcesCount({source: 'source', count: '5m'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10));

            assertEquals(2, expressionService.getCacheSize());
        }

        @Test
        @DisplayName("Should handle different contexts")
        void testCachingWithContext() {
            String expr = "#hasResourcesCount({source: 'source', count: '3m'})";

            expressionService.evaluate(expr, Map.of("param", "value1"), AbstractCondition.class, Duration.ofMinutes(10));
            expressionService.evaluate(expr, Map.of("param", "value2"), AbstractCondition.class, Duration.ofMinutes(10));

            assertEquals(2, expressionService.getCacheSize());
        }

        @Test
        @DisplayName("Should keep cached condition and condition-list values separate")
        void testConditionAndConditionListUseSeparateCacheEntries() {
            String expression = "#hasResourcesCount({source: 'source', count: '3m'})";

            AbstractCondition condition = expressionService.evaluate(
                    expression,
                    Map.of(),
                    AbstractCondition.class,
                    Duration.ofMinutes(10)
            );
            var conditions = expressionService.evaluateConditions(expression, Map.of());

            assertSame(condition, conditions.get(0));
            assertEquals(2, expressionService.getCacheSize());
        }

        @Test
        @DisplayName("Should invalidate condition lists through their member expression key")
        void testInvalidateByExpressionKey() {
            String firstExpression = "#hasResourcesCount({source: 'source', count: '3m'})";
            String secondExpression = "#areShips({planet: 'source', need: 'transporterSmall:5'})";

            expressionService.evaluateConditions(firstExpression + " | " + secondExpression, Map.of());
            assertEquals(3, expressionService.getCacheSize());

            expressionService.invalidateByKey(firstExpression);

            assertEquals(1, expressionService.getCacheSize());
        }
    }

    @Nested
    @DisplayName("Cache Invalidation Tests")
    class InvalidationTests {

        @Test
        @DisplayName("Should invalidate all")
        void testInvalidateAll() {
            expressionService.evaluate("#hasResourcesCount({source: 'source', count: '3m'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10));
            expressionService.evaluate("#areShips({planet: 'source', need: 'transporterSmall:5'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10));

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
                    expressionService.evaluate(null, Map.of(), AbstractCondition.class, Duration.ofMinutes(10))
            );
        }

        @Test
        @DisplayName("Should throw on empty expression")
        void testEmptyExpression() {
            assertThrows(IllegalArgumentException.class, () ->
                    expressionService.evaluate("", Map.of(), AbstractCondition.class, Duration.ofMinutes(10))
            );
        }

        @Test
        @DisplayName("Should throw on invalid SpEL expression")
        void testInvalidExpression() {
            assertThrows(IllegalStateException.class, () ->
                    expressionService.evaluate("#nonExistentFunction({param: 'value'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10))
            );
        }

        @Test
        @DisplayName("Should throw on missing required parameter")
        void testMissingRequiredParameter() {
            assertThrows(IllegalStateException.class, () ->
                    expressionService.evaluate("#hasResourcesCount({source: 'source'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10))
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

            assertInstanceOf(HasResourcesCountCondition.class, result);
            assertEquals(0, expressionService.getCacheSize());
        }

        @Test
        @DisplayName("Should not interfere with cached evaluation")
        void testNoCachedDoesNotInterfere() {
            String expr = "#hasResourcesCount({source: 'source', count: '3m'})";

            expressionService.evaluate(expr, Map.of(), AbstractCondition.class, Duration.ofMinutes(10));
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
            expressionService.evaluate("#hasResourcesCount({source: 'source', count: '3m'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10));
            expressionService.evaluate("#areShips({planet: 'source', need: 'transporterSmall:5'})", Map.of(), AbstractCondition.class, Duration.ofMinutes(10));

            String stats = expressionService.getCacheStats();
            assertNotNull(stats);
            assertTrue(stats.contains("2 entries"));
        }
    }
}
