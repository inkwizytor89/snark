package org.enoch.snark.expression;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing domain-specific strings into structured objects.
 * 
 * Supports parsing:
 * - Resources: "metal:100 crystal:50 deuterium:10"
 * - ShipsMap: "sg:5 bc:3 dt:10"
 * - Duration: "1H", "30M", "45S"
 * - Planet enums: "source", "target", "p[1:2:3]"
 */
@Slf4j
public class ExpressionParsingUtils {

    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("(\\w+):(\\w+)");
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([HMS])");

    /**
     * Parse a resources string into a map.
     * 
     * Format: "metal:100 crystal:50 deuterium:10"
     * 
     * @param resourcesStr The resources string
     * @return Map of resource type to quantity
     * @throws IllegalArgumentException if format is invalid
     */
    public static Map<String, String> parseResources(String resourcesStr) {
        if (resourcesStr == null || resourcesStr.isBlank()) {
            return new HashMap<>();
        }

        Map<String, String> resources = new HashMap<>();
        Matcher matcher = KEY_VALUE_PATTERN.matcher(resourcesStr);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            resources.put(key, value);
        }

        if (resources.isEmpty()) {
            log.warn("No resources parsed from string: '{}'", resourcesStr);
        }

        return resources;
    }

    /**
     * Parse a ships map string.
     * 
     * Format: "sg:5 bc:3 dt:10"
     * 
     * @param shipsStr The ships string
     * @return Map of ship type to quantity
     * @throws IllegalArgumentException if format is invalid
     */
    public static Map<String, Integer> parseShipsMap(String shipsStr) {
        if (shipsStr == null || shipsStr.isBlank()) {
            return new HashMap<>();
        }

        Map<String, Integer> ships = new HashMap<>();
        Matcher matcher = KEY_VALUE_PATTERN.matcher(shipsStr);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            try {
                int quantity = Integer.parseInt(value);
                ships.put(key, quantity);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse ship quantity for '{}': '{}'", key, value);
            }
        }

        if (ships.isEmpty()) {
            log.warn("No ships parsed from string: '{}'", shipsStr);
        }

        return ships;
    }

    /**
     * Parse a duration string into milliseconds.
     * 
     * Format: "1H" (1 hour), "30M" (30 minutes), "45S" (45 seconds)
     * 
     * @param durationStr The duration string
     * @return Duration in milliseconds
     * @throws IllegalArgumentException if format is invalid
     */
    public static long parseDurationToMillis(String durationStr) {
        if (durationStr == null || durationStr.isBlank()) {
            throw new IllegalArgumentException("Duration string cannot be null or empty");
        }

        Matcher matcher = DURATION_PATTERN.matcher(durationStr.toUpperCase());

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid duration format: '" + durationStr + "'. Expected format: 1H, 30M, 45S");
        }

        long value = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2);

        return switch (unit) {
            case "H" -> value * 60 * 60 * 1000;
            case "M" -> value * 60 * 1000;
            case "S" -> value * 1000;
            default -> throw new IllegalArgumentException("Unknown time unit: " + unit);
        };
    }

    /**
     * Parse a duration string into seconds.
     * 
     * @param durationStr The duration string
     * @return Duration in seconds
     */
    public static long parseDurationToSeconds(String durationStr) {
        return parseDurationToMillis(durationStr) / 1000;
    }

    /**
     * Parse a planet reference.
     * 
     * Can be:
     * - "source" or "target" (enum names)
     * - "p[1:2:3]" (coordinate notation)
     * 
     * @param planetStr The planet string
     * @return Parsed planet reference (for now, returns the input as-is)
     */
    public static String parsePlanet(String planetStr) {
        if (planetStr == null || planetStr.isBlank()) {
            throw new IllegalArgumentException("Planet string cannot be null or empty");
        }

        // Basic validation
        String trimmed = planetStr.trim();
        if (trimmed.equalsIgnoreCase("source") || trimmed.equalsIgnoreCase("target")) {
            return trimmed.toLowerCase();
        }

        if (trimmed.matches("p\\[\\d+:\\d+:\\d+\\]")) {
            return trimmed;
        }

        throw new IllegalArgumentException("Invalid planet format: '" + planetStr + "'. Expected 'source', 'target', or 'p[x:y:z]'");
    }

    /**
     * Parse a required parameter from a map.
     * 
     * @param params The parameter map
     * @param paramName The parameter name
     * @return The parameter value
     * @throws IllegalArgumentException if parameter is missing
     */
    public static String getRequiredParam(Map<String, Object> params, String paramName) {
        Object value = params.get(paramName);
        if (value == null) {
            throw new IllegalArgumentException("Required parameter '" + paramName + "' is missing");
        }
        return value.toString();
    }

    /**
     * Parse an optional parameter from a map.
     * 
     * @param params The parameter map
     * @param paramName The parameter name
     * @param defaultValue The default value if parameter is missing
     * @return The parameter value or default
     */
    public static String getOptionalParam(Map<String, Object> params, String paramName, String defaultValue) {
        Object value = params.get(paramName);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Convert a map to a readable string representation.
     * 
     * @param map The map to convert
     * @return A human-readable string
     */
    public static String mapToString(Map<String, ?> map) {
        return map.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .reduce((a, b) -> a + ", " + b)
            .orElse("{}");
    }
}
