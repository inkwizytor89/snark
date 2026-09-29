package org.enoch.snark.expression.coordinate.function;

import org.enoch.snark.instance.model.to.EventFleet;
import java.time.LocalDateTime;
import org.enoch.snark.common.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Filter for EventFleet objects - allows filtering by field values
 * and extracting specific fields from the filtered results.
 * 
 * <p>Example usage in SPEL expressions:</p>
 * <ul>
 * <li>#filterField(eventFleets, 'mission', Map.of('isHostile', true)) - returns missions of hostile fleets</li>
 * <li>#filterField(eventFleets, 'countDown', Map.of('mission', 'ATTACK')) - returns countdowns for attack missions</li>
 * <li>#filterField(eventFleets, 'destCoords', Map.of()) - returns destination coordinates for all fleets</li>
 * <li>#filterField(eventFleets, 'arrivalTime', Map.of('arrivalTime', '1H-2H')) - returns arrival times between 1 and 2 hours from now</li>
 * </ul>
 */
public class EventFleetFilter {
    
    /**
     * Filters the list of EventFleet and returns the selected field.
     * 
     * @param eventFleets the list to filter
     * @param fieldName the field to return (e.g., "countDown", "mission", "isHostile")
     * @param filters the map of filters (e.g., Map.of("mission", "TRANSPORT", "isHostile", "true"))
     * @return the list of values from the selected field
     */
    public static List<?> filterField(List<EventFleet> eventFleets, String fieldName, Map<String, Object> filters) {
        if (eventFleets == null || eventFleets.isEmpty()) {
            return List.of();
        }
        
        return eventFleets.stream()
                .filter(fleet -> matchesFilters(fleet, filters))
                .map(fleet -> getFieldValue(fleet, fieldName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Returns the value of the EventFleet field via reflection.
     */
    private static Object getFieldValue(EventFleet fleet, String fieldName) {
        if (fleet == null || fieldName == null) {
            return null;
        }
        
        return switch (fieldName.toLowerCase()) {
            case "countdown", "countdown_str" -> fleet.countDown;
            case "arrivaltime", "arrival_time" -> fleet.arrivalTime;
            case "mission" -> fleet.mission;
            case "originfleet", "origin_fleet" -> fleet.originFleet;
            case "coordsorigin", "coords_origin" -> fleet.coordsOrigin;
            case "detailsfleet", "details_fleet" -> fleet.detailsFleet;
            case "iconmovement", "icon_movement" -> fleet.iconMovement;
            case "destfleet", "dest_fleet" -> fleet.destFleet;
            case "destcoords", "dest_coords" -> fleet.destCoords;
            case "sendprobe", "send_probe" -> fleet.sendProbe;
            case "sendmail", "send_mail" -> fleet.sendMail;
            case "ishostile", "is_hostile" -> fleet.isHostile;
            default -> null;
        };
    }
    
    /**
     * Checks if the EventFleet matches all filters.
     */
    private static boolean matchesFilters(EventFleet fleet, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }
        
        return filters.entrySet().stream()
                .allMatch(entry -> matchesFilter(fleet, entry.getKey(), entry.getValue()));
    }
    
    /**
     * Checks if the given EventFleet field matches the filter.
     */
    private static boolean matchesFilter(EventFleet fleet, String fieldName, Object expectedValue) {
        Object actualValue = getFieldValue(fleet, fieldName);
        if (actualValue == null) {
            return false;
        }
        
        if (actualValue instanceof LocalDateTime) {
            return matchesLocalDateTime((LocalDateTime) actualValue, expectedValue);
        } else if (expectedValue instanceof String) {
            return actualValue.toString().equalsIgnoreCase(expectedValue.toString());
        } else if (expectedValue instanceof Boolean && actualValue instanceof Boolean) {
            return actualValue.equals(expectedValue);
        } else {
            return actualValue.equals(expectedValue);
        }
    }
    
    /**
     * Checks if the LocalDateTime field matches the filter with operator support.
     * Supports operators: '>' (after), '<' (before), '=' (equal). Defaults to '>' if no operator.
     * Also supports range: 'min-max' (e.g., '1H-2H' means after 1H and before 2H).
     * Expected value is parsed as a duration string (e.g., ">1H", "30M", "1H-2H").
     */
    private static boolean matchesLocalDateTime(LocalDateTime actual, Object expectedValue) {
        String str = expectedValue.toString();
        if (str.contains("-")) {
            // Handle range: min-max
            String[] parts = str.split("-", 2);
            String minStr = parts[0];
            String maxStr = parts[1];
            Duration minDuration = new Duration(minStr);
            Duration maxDuration = new Duration(maxStr);
            java.time.Duration minDur = minDuration.getValue();
            java.time.Duration maxDur = maxDuration.getValue();
            LocalDateTime minThreshold = LocalDateTime.now().plus(minDur);
            LocalDateTime maxThreshold = LocalDateTime.now().plus(maxDur);
            return actual.isAfter(minThreshold) && actual.isBefore(maxThreshold);
        } else {
            // Handle single operator
            char operator = '>';
            String durationStr = str;
            if (str.startsWith(">")) {
                operator = '>';
                durationStr = str.substring(1);
            } else if (str.startsWith("<")) {
                operator = '<';
                durationStr = str.substring(1);
            } else if (str.startsWith("=")) {
                operator = '=';
                durationStr = str.substring(1);
            }
            Duration duration = new Duration(durationStr);
            java.time.Duration dur = duration.getValue();
            LocalDateTime threshold = LocalDateTime.now().plus(dur);
            return switch (operator) {
                case '>' -> actual.isAfter(threshold);
                case '<' -> actual.isBefore(threshold);
                case '=' -> actual.isEqual(threshold);
                default -> false;
            };
        }
    }
}
