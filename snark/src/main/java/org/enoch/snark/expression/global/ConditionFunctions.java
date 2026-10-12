package org.enoch.snark.expression.global;

import lombok.extern.slf4j.Slf4j;
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
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Condition function implementations for SpEL expressions.
 *
 * These static methods are registered as SpEL functions via {@link GlobalSpelFunctionRegistry}
 * and return real condition instances used by the runtime.
 */
@Slf4j
public class ConditionFunctions {

    public static AbstractCondition hasResourcesCount(Map<String, Object> params) {
        log.debug("Creating hasResourcesCount condition with params: {}", ExpressionParsingUtils.mapToString(params));

        String planetValue = firstPresentRequired(params, "planet", "source");
        String count = firstPresentRequired(params, "count", "resourcesCount");
        return new ResourceCountCondition(count, parsePlanetValue(planetValue));
    }

    public static AbstractCondition hasResources(Map<String, Object> params) {
        log.debug("Creating hasResources condition with params: {}", ExpressionParsingUtils.mapToString(params));

        Planet planet = parsePlanetValue(firstPresentRequired(params, "planet", "source"));
        Resources have = Resources.parse(firstPresentRequired(params, "have", "resources"));
        Resources leave = Resources.parse(firstPresentOptional(params, "nothing", "leave", "leaveResources"));
        return new ResourceCondition(planet, have, leave);
    }

    public static AbstractCondition areShips(Map<String, Object> params) {
        log.debug("Creating areShips condition with params: {}", ExpressionParsingUtils.mapToString(params));

        Planet planet = parsePlanetValue(firstPresentRequired(params, "planet", "source"));
        ShipsMap need = ShipsMap.parse(firstPresentRequired(params, "need", "shipsMap"));
        ShipsMap leave = ShipsMap.parse(firstPresentOptional(params, "none", "leave"));
        return new ShipsCondition(need, leave, planet);
    }

    public static AbstractCondition isFleetExpired(Map<String, Object> params) {
        log.debug("Creating isFleetExpired condition with params: {}", ExpressionParsingUtils.mapToString(params));

        Planet source = parsePlanetValue(firstPresentRequired(params, "source", "planet"));
        String missions = normalizeMissions(firstPresentRequired(params, "missions", "blockingMissions"));
        String duration = firstPresentOptional(params, "1H", "timeout", "duration");
        return new FleetExpiredCondition(source, missions, duration);
    }

    public static AbstractCondition isExpressionTrue(Map<String, Object> params) {
        log.debug("Creating isExpressionTrue condition with params: {}", ExpressionParsingUtils.mapToString(params));

        String term = firstPresentRequired(params, "term");
        String operator = firstPresentRequired(params, "op", "operator");
        String value = firstPresentRequired(params, "value");
        return new ExpressionCondition(term, operator, value);
    }

    public static AbstractCondition hasTechnology(Map<String, Object> params) {
        throw new UnsupportedOperationException("hasTechnology is not supported yet because there is no concrete TechnologyCondition implementation");
    }

    public static AbstractCondition hasNoMissions(Map<String, Object> params) {
        log.debug("Creating hasNoMissions condition with params: {}", ExpressionParsingUtils.mapToString(params));

        Planet source = parsePlanetValue(firstPresentRequired(params, "source", "planet"));
        String missions = normalizeMissions(firstPresentOptional(params, allMissions(), "missions", "blockingMissions"));
        String duration = firstPresentOptional(params, "1H", "timeout", "duration");
        return new NoMissionsCondition(source, missions, duration);
    }

    public static AbstractCondition hasFleetSlots(Map<String, Object> params) {
        log.debug("Creating hasFleetSlots condition with params: {}", ExpressionParsingUtils.mapToString(params));

        Integer freeSlots = Integer.valueOf(firstPresentRequired(params, "freeSlots", "required"));
        return new FleetSlotCondition(freeSlots);
    }

    public static AbstractCondition isAttackNotExpired(Map<String, Object> params) {
        log.debug("Creating isAttackNotExpired condition with params: {}", ExpressionParsingUtils.mapToString(params));

        Planet target = parsePlanetValue(firstPresentRequired(params, "target"));
        Long seconds = ExpressionParsingUtils.parseDurationToSeconds(firstPresentOptional(params, "1H", "timeout", "duration"));
        return new AttackNotExpiredCondition(seconds, false, target);
    }

    public static AbstractCondition isSpyNotExpired(Map<String, Object> params) {
        log.debug("Creating isSpyNotExpired condition with params: {}", ExpressionParsingUtils.mapToString(params));

        Planet target = parsePlanetValue(firstPresentRequired(params, "target"));
        Long seconds = ExpressionParsingUtils.parseDurationToSeconds(firstPresentOptional(params, "1H", "timeout", "duration"));
        return new SpyNotExpiredCondition(seconds, false, target);
    }

    public static AbstractCondition isModifiedAfter(Map<String, Object> params) {
        log.debug("Creating isModifiedAfter condition with params: {}", ExpressionParsingUtils.mapToString(params));

        String firstTerm = firstPresentRequired(params, "first", "firstTerm");
        String secondTerm = firstPresentRequired(params, "second", "secondTerm");
        return new IsModifiedAfterCondition(firstTerm, secondTerm);
    }

    private static Planet parsePlanetValue(String value) {
        Planet planet = Planet.parse(value);
        if (planet == null) {
            throw new IllegalArgumentException("Planet value can not be null or blank");
        }
        return planet;
    }

    private static String firstPresentRequired(Map<String, Object> params, String... names) {
        for (String name : names) {
            Object value = params.get(name);
            if (value != null) {
                return value.toString();
            }
        }
        throw new IllegalArgumentException("Required parameter missing. Expected one of: " + String.join(", ", names));
    }

    private static String firstPresentOptional(Map<String, Object> params, String defaultValue, String... names) {
        for (String name : names) {
            Object value = params.get(name);
            if (value != null) {
                return value.toString();
            }
        }
        return defaultValue;
    }

    private static String normalizeMissions(String missions) {
        return missions.replace("|", ";");
    }

    private static String allMissions() {
        return Arrays.stream(Mission.values())
                .filter(mission -> !Mission.UNKNOWN.equals(mission))
                .map(Enum::name)
                .collect(Collectors.joining(";"));
    }
}
