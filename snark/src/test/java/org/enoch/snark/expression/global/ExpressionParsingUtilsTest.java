package org.enoch.snark.expression.global;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ExpressionParsingUtils.
 */
@DisplayName("ExpressionParsingUtils Tests")
class ExpressionParsingUtilsTest {

    @Nested
    @DisplayName("Resources Parsing Tests")
    class ResourcesParsingTests {

        @Test
        @DisplayName("Should parse single resource")
        void testParseSingleResource() {
            Map<String, String> result = ExpressionParsingUtils.parseResources("metal:100");
            
            assertEquals(1, result.size());
            assertEquals("100", result.get("metal"));
        }

        @Test
        @DisplayName("Should parse multiple resources")
        void testParseMultipleResources() {
            Map<String, String> result = ExpressionParsingUtils.parseResources("metal:100 crystal:50 deuterium:10");
            
            assertEquals(3, result.size());
            assertEquals("100", result.get("metal"));
            assertEquals("50", result.get("crystal"));
            assertEquals("10", result.get("deuterium"));
        }

        @Test
        @DisplayName("Should handle empty string")
        void testParseEmptyResources() {
            Map<String, String> result = ExpressionParsingUtils.parseResources("");
            
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle null")
        void testParseNullResources() {
            Map<String, String> result = ExpressionParsingUtils.parseResources(null);
            
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Ships Map Parsing Tests")
    class ShipsMapParsingTests {

        @Test
        @DisplayName("Should parse single ship type")
        void testParseSingleShip() {
            Map<String, Integer> result = ExpressionParsingUtils.parseShipsMap("sg:5");
            
            assertEquals(1, result.size());
            assertEquals(5, result.get("sg"));
        }

        @Test
        @DisplayName("Should parse multiple ship types")
        void testParseMultipleShips() {
            Map<String, Integer> result = ExpressionParsingUtils.parseShipsMap("sg:5 bc:3 dt:10");
            
            assertEquals(3, result.size());
            assertEquals(5, result.get("sg"));
            assertEquals(3, result.get("bc"));
            assertEquals(10, result.get("dt"));
        }

        @Test
        @DisplayName("Should handle empty string")
        void testParseEmptyShips() {
            Map<String, Integer> result = ExpressionParsingUtils.parseShipsMap("");
            
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle null")
        void testParseNullShips() {
            Map<String, Integer> result = ExpressionParsingUtils.parseShipsMap(null);
            
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Duration Parsing Tests")
    class DurationParsingTests {

        @Test
        @DisplayName("Should parse hours")
        void testParseHours() {
            long ms = ExpressionParsingUtils.parseDurationToMillis("1H");
            assertEquals(60 * 60 * 1000, ms);
        }

        @Test
        @DisplayName("Should parse minutes")
        void testParseMinutes() {
            long ms = ExpressionParsingUtils.parseDurationToMillis("30M");
            assertEquals(30 * 60 * 1000, ms);
        }

        @Test
        @DisplayName("Should parse seconds")
        void testParseSeconds() {
            long ms = ExpressionParsingUtils.parseDurationToMillis("45S");
            assertEquals(45 * 1000, ms);
        }

        @Test
        @DisplayName("Should parse to seconds")
        void testParseDurationToSeconds() {
            long sec = ExpressionParsingUtils.parseDurationToSeconds("1M");
            assertEquals(60, sec);
        }

        @Test
        @DisplayName("Should throw on invalid format")
        void testInvalidDurationFormat() {
            assertThrows(IllegalArgumentException.class, () ->
                ExpressionParsingUtils.parseDurationToMillis("invalid")
            );
        }

        @Test
        @DisplayName("Should throw on null")
        void testNullDuration() {
            assertThrows(IllegalArgumentException.class, () ->
                ExpressionParsingUtils.parseDurationToMillis(null)
            );
        }

        @Test
        @DisplayName("Should throw on empty")
        void testEmptyDuration() {
            assertThrows(IllegalArgumentException.class, () ->
                ExpressionParsingUtils.parseDurationToMillis("")
            );
        }

        @Test
        @DisplayName("Should handle case insensitivity")
        void testCaseInsensitive() {
            long ms1 = ExpressionParsingUtils.parseDurationToMillis("1h");
            long ms2 = ExpressionParsingUtils.parseDurationToMillis("1H");
            assertEquals(ms1, ms2);
        }
    }

    @Nested
    @DisplayName("Planet Parsing Tests")
    class PlanetParsingTests {

        @Test
        @DisplayName("Should parse 'source' keyword")
        void testParsePlanetSource() {
            String result = ExpressionParsingUtils.parsePlanet("source");
            assertEquals("source", result);
        }

        @Test
        @DisplayName("Should parse 'target' keyword")
        void testParsePlanetTarget() {
            String result = ExpressionParsingUtils.parsePlanet("target");
            assertEquals("target", result);
        }

        @Test
        @DisplayName("Should parse coordinate notation")
        void testParsePlanetCoordinate() {
            String result = ExpressionParsingUtils.parsePlanet("p[1:2:3]");
            assertEquals("p[1:2:3]", result);
        }

        @Test
        @DisplayName("Should handle case insensitivity for keywords")
        void testParsePlanetCaseInsensitive() {
            String result1 = ExpressionParsingUtils.parsePlanet("SOURCE");
            String result2 = ExpressionParsingUtils.parsePlanet("Source");
            
            assertEquals("source", result1);
            assertEquals("source", result2);
        }

        @Test
        @DisplayName("Should throw on invalid format")
        void testInvalidPlanetFormat() {
            assertThrows(IllegalArgumentException.class, () ->
                ExpressionParsingUtils.parsePlanet("invalid_planet")
            );
        }

        @Test
        @DisplayName("Should throw on null")
        void testNullPlanet() {
            assertThrows(IllegalArgumentException.class, () ->
                ExpressionParsingUtils.parsePlanet(null)
            );
        }

        @Test
        @DisplayName("Should throw on empty")
        void testEmptyPlanet() {
            assertThrows(IllegalArgumentException.class, () ->
                ExpressionParsingUtils.parsePlanet("")
            );
        }
    }

    @Nested
    @DisplayName("Parameter Extraction Tests")
    class ParameterExtractionTests {

        @Test
        @DisplayName("Should get required parameter")
        void testGetRequiredParam() {
            Map<String, Object> params = Map.of("key", "value");
            String result = ExpressionParsingUtils.getRequiredParam(params, "key");
            
            assertEquals("value", result);
        }

        @Test
        @DisplayName("Should throw on missing required parameter")
        void testMissingRequiredParam() {
            Map<String, Object> params = Map.of();
            
            assertThrows(IllegalArgumentException.class, () ->
                ExpressionParsingUtils.getRequiredParam(params, "key")
            );
        }

        @Test
        @DisplayName("Should get optional parameter with default")
        void testGetOptionalParam() {
            Map<String, Object> params = Map.of("key", "value");
            String result = ExpressionParsingUtils.getOptionalParam(params, "key", "default");
            
            assertEquals("value", result);
        }

        @Test
        @DisplayName("Should return default for missing optional parameter")
        void testGetOptionalParamDefault() {
            Map<String, Object> params = Map.of();
            String result = ExpressionParsingUtils.getOptionalParam(params, "key", "default");
            
            assertEquals("default", result);
        }
    }

    @Nested
    @DisplayName("Map to String Tests")
    class MapToStringTests {

        @Test
        @DisplayName("Should convert map to string")
        void testMapToString() {
            Map<String, Object> map = Map.of("key1", "value1", "key2", "value2");
            String result = ExpressionParsingUtils.mapToString(map);
            
            assertNotNull(result);
            assertTrue(result.contains("key1"));
            assertTrue(result.contains("value1"));
        }

        @Test
        @DisplayName("Should handle empty map")
        void testMapToStringEmpty() {
            Map<String, Object> map = Map.of();
            String result = ExpressionParsingUtils.mapToString(map);
            
            assertEquals("{}", result);
        }
    }
}
