package org.enoch.snark.expression.global;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GlobalExpressionService.
 * 
 * Tests cover:
 * - Basic expression evaluation
 * - Caching behavior
 * - TTL expiration
 * - Cache invalidation
 * - Error handling
 */
@DisplayName("GlobalExpressionService Tests")
class GlobalExpressionServiceTest {

    private GlobalExpressionService expressionService;

    @BeforeEach
    void setUp() {
        expressionService = new GlobalExpressionService();
        expressionService.invalidateAll(); // Clear cache before each test
    }

    @Nested
    @DisplayName("Condition Function Tests")
    class ConditionFunctionsTests {

        @Test
        @DisplayName("Should evaluate resourceCount condition")
        void testResourceCountCondition() {
            String expr = "#resourceCount({source: 'source', count: '3m'})";
            
            ConditionDto result = expressionService.evaluate(
                expr,
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );

            assertNotNull(result);
            assertEquals("RESOURCE_COUNT", result.getType());
            assertEquals("source", result.getParameter("source"));
            assertEquals("3m", result.getParameter("count"));
        }

        @Test
        @DisplayName("Should evaluate resource condition with leave amount")
        void testResourceCondition() {
            String expr = "#resource({planet: 'target', have: 'metal:100 crystal:50', leave: 'metal:10'})";
            
            ConditionDto result = expressionService.evaluate(
                expr,
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );

            assertNotNull(result);
            assertEquals("RESOURCE", result.getType());
            assertEquals("target", result.getParameter("planet"));
            assertNotNull(result.getParameter("have"));
            assertNotNull(result.getParameter("leave"));
        }

        @Test
        @DisplayName("Should evaluate ships condition without leave")
        void testShipsCondition() {
            String expr = "#ships({planet: 'source', need: 'sg:5 bc:3'})";
            
            ConditionDto result = expressionService.evaluate(
                expr,
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );

            assertNotNull(result);
            assertEquals("SHIPS", result.getType());
            assertEquals("source", result.getParameter("planet"));
            assertNotNull(result.getParameter("need"));
        }

        @Test
        @DisplayName("Should evaluate fleetExpired condition")
        void testFleetExpiredCondition() {
            String expr = "#fleetExpired({source: 'source', missions: 'TRANSPORT|ATTACK', timeout: '30M'})";
            
            ConditionDto result = expressionService.evaluate(
                expr,
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );

            assertNotNull(result);
            assertEquals("FLEET_EXPIRED", result.getType());
            assertEquals("source", result.getParameter("source"));
            assertEquals("TRANSPORT|ATTACK", result.getParameter("missions"));
            assertEquals(30 * 60 * 1000, result.getParameter("timeoutMs"));
        }

        @Test
        @DisplayName("Should evaluate expr condition")
        void testExprCondition() {
            String expr = "#expr({term: 'fuel_level', op: 'eq', value: 'ready'})";
            
            ConditionDto result = expressionService.evaluate(
                expr,
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );

            assertNotNull(result);
            assertEquals("EXPR", result.getType());
            assertEquals("fuel_level", result.getParameter("term"));
            assertEquals("eq", result.getParameter("operator"));
            assertEquals("ready", result.getParameter("value"));
        }

        @Test
        @DisplayName("Should evaluate tech condition")
        void testTechCondition() {
            String expr = "#tech({source: 'source', tech: 'IMPULSE', level: '5'})";
            
            ConditionDto result = expressionService.evaluate(
                expr,
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );

            assertNotNull(result);
            assertEquals("TECH", result.getType());
            assertEquals("source", result.getParameter("source"));
            assertEquals("IMPULSE", result.getParameter("technology"));
            assertEquals(5L, result.getParameter("level"));
        }

        @Test
        @DisplayName("Should evaluate noMissions condition")
        void testNoMissionsCondition() {
            String expr = "#noMissions({planet: 'target'})";
            
            ConditionDto result = expressionService.evaluate(
                expr,
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );

            assertNotNull(result);
            assertEquals("NO_MISSIONS", result.getType());
            assertEquals("target", result.getParameter("planet"));
        }

        @Test
        @DisplayName("Should evaluate fleetSlot condition")
        void testFleetSlotCondition() {
            String expr = "#fleetSlot({planet: 'source', required: '5'})";
            
            ConditionDto result = expressionService.evaluate(
                expr,
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );

            assertNotNull(result);
            assertEquals("FLEET_SLOT", result.getType());
            assertEquals("source", result.getParameter("planet"));
            assertEquals(5, result.getParameter("required"));
        }

        @Test
        @DisplayName("Should evaluate attackNotExpired condition")
        void testAttackNotExpiredCondition() {
            String expr = "#attackNotExpired({target: 'target', timeout: '1H'})";
            
            ConditionDto result = expressionService.evaluate(
                expr,
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );

            assertNotNull(result);
            assertEquals("ATTACK_NOT_EXPIRED", result.getType());
            assertEquals("target", result.getParameter("target"));
            assertEquals(60 * 60 * 1000, result.getParameter("timeoutMs"));
        }

        @Test
        @DisplayName("Should evaluate spyNotExpired condition")
        void testSpyNotExpiredCondition() {
            String expr = "#spyNotExpired({target: 'target'})";
            
            ConditionDto result = expressionService.evaluate(
                expr,
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );

            assertNotNull(result);
            assertEquals("SPY_NOT_EXPIRED", result.getType());
            assertEquals("target", result.getParameter("target"));
            assertEquals(60 * 60 * 1000, result.getParameter("timeoutMs")); // default 1H
        }

        @Test
        @DisplayName("Should evaluate modifiedAfter condition")
        void testModifiedAfterCondition() {
            String expr = "#modifiedAfter({target: 'source', since: '10M'})";
            
            ConditionDto result = expressionService.evaluate(
                expr,
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );

            assertNotNull(result);
            assertEquals("IS_MODIFIED_AFTER", result.getType());
            assertEquals("source", result.getParameter("target"));
            assertEquals(10 * 60 * 1000, result.getParameter("sinceMs"));
        }
    }

    @Nested
    @DisplayName("Caching Tests")
    class CachingTests {

        @Test
        @DisplayName("Should cache identical expressions")
        void testCaching() {
            String expr = "#resourceCount({source: 'source', count: '3m'})";
            
            // First evaluation - should miss cache
            assertEquals(0, expressionService.getCacheSize());
            
            ConditionDto result1 = expressionService.evaluate(
                expr,
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );
            
            assertEquals(1, expressionService.getCacheSize());
            
            // Second evaluation - should hit cache
            ConditionDto result2 = expressionService.evaluate(
                expr,
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );
            
            assertEquals(1, expressionService.getCacheSize()); // Still 1, not added
            assertEquals(result1, result2);
        }

        @Test
        @DisplayName("Should not cache different expressions")
        void testNoCacheForDifferentExpressions() {
            String expr1 = "#resourceCount({source: 'source', count: '3m'})";
            String expr2 = "#resourceCount({source: 'source', count: '5m'})";
            
            expressionService.evaluate(expr1, Map.of(), ConditionDto.class, Duration.ofMinutes(10), "conditions");
            expressionService.evaluate(expr2, Map.of(), ConditionDto.class, Duration.ofMinutes(10), "conditions");
            
            assertEquals(2, expressionService.getCacheSize());
        }

        @Test
        @DisplayName("Should handle different contexts")
        void testCachingWithContext() {
            String expr = "#resourceCount({source: 'source', count: '3m'})";
            
            ConditionDto result1 = expressionService.evaluate(
                expr,
                Map.of("param", "value1"),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );
            
            ConditionDto result2 = expressionService.evaluate(
                expr,
                Map.of("param", "value2"),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );
            
            // Different context = different cache keys
            assertEquals(2, expressionService.getCacheSize());
        }
    }

    @Nested
    @DisplayName("Cache Invalidation Tests")
    class InvalidationTests {

        @Test
        @DisplayName("Should invalidate by domain")
        void testInvalidateByDomain() {
            expressionService.evaluate(
                "#resourceCount({source: 'source', count: '3m'})",
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );
            
            expressionService.evaluate(
                "#ships({planet: 'source', need: 'sg:5'})",
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "coordinates"
            );
            
            assertEquals(2, expressionService.getCacheSize());
            
            expressionService.invalidateByDomain("conditions");
            assertEquals(1, expressionService.getCacheSize());
        }

        @Test
        @DisplayName("Should invalidate all")
        void testInvalidateAll() {
            expressionService.evaluate(
                "#resourceCount({source: 'source', count: '3m'})",
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );
            
            expressionService.evaluate(
                "#ships({planet: 'source', need: 'sg:5'})",
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );
            
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
                expressionService.evaluate(
                    null,
                    Map.of(),
                    ConditionDto.class,
                    Duration.ofMinutes(10),
                    "conditions"
                )
            );
        }

        @Test
        @DisplayName("Should throw on empty expression")
        void testEmptyExpression() {
            assertThrows(IllegalArgumentException.class, () ->
                expressionService.evaluate(
                    "",
                    Map.of(),
                    ConditionDto.class,
                    Duration.ofMinutes(10),
                    "conditions"
                )
            );
        }

        @Test
        @DisplayName("Should throw on invalid SpEL expression")
        void testInvalidExpression() {
            assertThrows(IllegalStateException.class, () ->
                expressionService.evaluate(
                    "#nonExistentFunction({param: 'value'})",
                    Map.of(),
                    ConditionDto.class,
                    Duration.ofMinutes(10),
                    "conditions"
                )
            );
        }

        @Test
        @DisplayName("Should throw on missing required parameter")
        void testMissingRequiredParameter() {
            assertThrows(IllegalStateException.class, () ->
                expressionService.evaluate(
                    "#resourceCount({source: 'source'})",  // missing 'count'
                    Map.of(),
                    ConditionDto.class,
                    Duration.ofMinutes(10),
                    "conditions"
                )
            );
        }
    }

    @Nested
    @DisplayName("NoCached Evaluation Tests")
    class NoCachedTests {

        @Test
        @DisplayName("Should evaluate without caching")
        void testNoCachedEvaluation() {
            String expr = "#resourceCount({source: 'source', count: '3m'})";
            
            ConditionDto result = expressionService.evaluateNoCached(
                expr,
                Map.of(),
                ConditionDto.class
            );
            
            assertNotNull(result);
            assertEquals(0, expressionService.getCacheSize()); // Not cached
        }

        @Test
        @DisplayName("Should not interfere with cached evaluation")
        void testNoCachedDoesNotInterfere() {
            String expr = "#resourceCount({source: 'source', count: '3m'})";
            
            // First - cached
            expressionService.evaluate(expr, Map.of(), ConditionDto.class, Duration.ofMinutes(10), "conditions");
            assertEquals(1, expressionService.getCacheSize());
            
            // Second - not cached, but shouldn't clear the cache
            expressionService.evaluateNoCached(expr, Map.of(), ConditionDto.class);
            assertEquals(1, expressionService.getCacheSize());
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should report cache stats")
        void testCacheStats() {
            expressionService.evaluate(
                "#resourceCount({source: 'source', count: '3m'})",
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "conditions"
            );
            
            expressionService.evaluate(
                "#ships({planet: 'source', need: 'sg:5'})",
                Map.of(),
                ConditionDto.class,
                Duration.ofMinutes(10),
                "coordinates"
            );
            
            String stats = expressionService.getCacheStats();
            assertNotNull(stats);
            assertTrue(stats.contains("2 entries"));
        }
    }
}
