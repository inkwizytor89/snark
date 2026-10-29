package org.enoch.snark.expression.function.eventfleet;

import org.enoch.snark.common.time.Duration;
import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class EventFleetFunctions {

    private static final Set<String> FILTER_NAMES = Set.of(
            "arrivalTimeLessThan",
            "arrivalTimeGreaterThan",
            "isHostile",
            "countGreaterThan",
            "countLessThan",
            "mission"
    );

    private EventFleetFunctions() {
    }

    public static List<EventFleet> filter(Map<String, ?> filters) {
        validateFilterNames(filters);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime arrivalTimeLessThan = durationThreshold(filters, "arrivalTimeLessThan", now);
        LocalDateTime arrivalTimeGreaterThan = durationThreshold(filters, "arrivalTimeGreaterThan", now);
        Boolean isHostile = booleanFilter(filters, "isHostile");
        Long countGreaterThan = countFilter(filters, "countGreaterThan");
        Long countLessThan = countFilter(filters, "countLessThan");
        Set<Mission> missions = missionFilter(filters);

        List<EventFleet> eventFleets = Navigator.getInstance().getEventFleetList();
        if (eventFleets == null || eventFleets.isEmpty()) {
            return List.of();
        }

        return eventFleets.stream()
                .filter(Objects::nonNull)
                .filter(fleet -> arrivalTimeLessThan == null
                        || fleet.arrivalTime != null && fleet.arrivalTime.isBefore(arrivalTimeLessThan))
                .filter(fleet -> arrivalTimeGreaterThan == null
                        || fleet.arrivalTime != null && fleet.arrivalTime.isAfter(arrivalTimeGreaterThan))
                .filter(fleet -> isHostile == null || fleet.isHostile == isHostile)
                .filter(fleet -> countGreaterThan == null
                        || fleet.detailsFleet != null && Long.parseLong(fleet.detailsFleet) > countGreaterThan)
                .filter(fleet -> countLessThan == null
                        || fleet.detailsFleet != null && Long.parseLong(fleet.detailsFleet) < countLessThan)
                .filter(fleet -> missions == null || missions.contains(fleet.mission))
                .toList();
    }

    public static Planet endingPlanet(EventFleet eventFleet) {
        return Objects.requireNonNull(eventFleet, "eventFleet must not be null").getEndingPlanet();
    }

    private static void validateFilterNames(Map<String, ?> filters) {
        if (filters == null) {
            return;
        }
        for (String filterName : filters.keySet()) {
            if (!FILTER_NAMES.contains(filterName)) {
                throw new IllegalArgumentException("Unknown event fleet filter: " + filterName);
            }
        }
    }

    private static LocalDateTime durationThreshold(
            Map<String, ?> filters,
            String filterName,
            LocalDateTime now
    ) {
        if (filters == null || !filters.containsKey(filterName)) {
            return null;
        }
        Object value = filters.get(filterName);
        if (!(value instanceof String duration)) {
            throw new IllegalArgumentException(filterName + " must be a duration string");
        }
        return now.plus(new Duration(duration).getValue());
    }

    private static Boolean booleanFilter(Map<String, ?> filters, String filterName) {
        if (filters == null || !filters.containsKey(filterName)) {
            return null;
        }
        Object value = filters.get(filterName);
        if (!(value instanceof Boolean result)) {
            throw new IllegalArgumentException(filterName + " must be a boolean");
        }
        return result;
    }

    private static Long countFilter(Map<String, ?> filters, String filterName) {
        if (filters == null || !filters.containsKey(filterName)) {
            return null;
        }
        Object value = filters.get(filterName);
        try {
            if (value instanceof Number || value instanceof String) {
                return new BigDecimal(value.toString()).longValueExact();
            }
        } catch (NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException(filterName + " must be an integer", e);
        }
        throw new IllegalArgumentException(filterName + " must be an integer");
    }

    private static Set<Mission> missionFilter(Map<String, ?> filters) {
        if (filters == null || !filters.containsKey("mission")) {
            return null;
        }
        Object value = filters.get("mission");
        if (!(value instanceof String missionNames)) {
            throw new IllegalArgumentException("mission must be a semicolon-separated string");
        }

        Set<Mission> missions = Arrays.stream(missionNames.split(";"))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(name -> {
                    try {
                        return Mission.valueOf(name.toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Unknown mission: " + name, e);
                    }
                })
                .collect(Collectors.toSet());
        if (missions.isEmpty()) {
            throw new IllegalArgumentException("mission must contain at least one mission name");
        }
        return missions;
    }
}
