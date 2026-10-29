package org.enoch.snark.expression;

import lombok.extern.slf4j.Slf4j;
import org.enoch.snark.expression.function.condition.ConditionFunctions;
import org.enoch.snark.expression.function.coordinate.Base;
import org.enoch.snark.expression.function.coordinate.Colonies;
import org.enoch.snark.expression.function.coordinate.SpelFunctionWrapper;
import org.enoch.snark.expression.function.eventfleet.EventFleetFunctions;
import org.enoch.snark.expression.function.list.ListFunctions;
import org.enoch.snark.instance.model.to.EventFleet;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;
import java.util.Map;

/**
 * Registry for SpEL functions available across all domains.
 * 
 * This class centralizes registration of all SpEL functions that can be used in expressions.
 * Function implementations are grouped by their return type under the function package.
 * 
 * To add a new function:
 * 1. Create a static method in a return-type function package
 * 2. Register it here using context.registerFunction()
 * 
 * Example:
 * <pre>
 * context.registerFunction("myFunction", MyFunctions.class.getDeclaredMethod("myFunction", String.class));
 * </pre>
 */
@Slf4j
public class FunctionRegistry {

    /**
     * Register all available SpEL functions.
     *
     * @param context The StandardEvaluationContext to register functions in
     * @throws NoSuchMethodException if a method cannot be found (configuration error)
     */
    public static void registerFunctions(StandardEvaluationContext context) throws NoSuchMethodException {
        log.debug("Registering SpEL functions");

        // ============ COORDINATE FUNCTIONS ============
        // These functions return coordinates/planets/moons
        registerCoordinateFunctions(context);

        // ============ CONDITION FUNCTIONS ============
        // These functions create AbstractCondition instances
        registerConditionFunctions(context);

        // ============ RESOURCE FUNCTIONS ============
        // These functions work with resources
        registerResourceFunctions(context);

        log.debug("SpEL functions registered successfully");
    }

    /** Register coordinate-related functions. */
    public static void registerCoordinateFunctions(StandardEvaluationContext context) throws NoSuchMethodException {
        log.debug("Registering coordinate functions");
        context.registerFunction("all_planets", Colonies.class.getDeclaredMethod("planets"));
        context.registerFunction("planets", SpelFunctionWrapper.class.getDeclaredMethod("planets", Object.class));
        context.registerFunction("all_moons", Colonies.class.getDeclaredMethod("moons"));
        context.registerFunction("moons", SpelFunctionWrapper.class.getDeclaredMethod("moons", Object.class));

        context.registerFunction("all", Colonies.class.getDeclaredMethod("all"));
        context.registerFunction("none", Colonies.class.getDeclaredMethod("none"));
        context.registerFunction("allPositionsFunc", Colonies.class.getDeclaredMethod("allPositions"));
        context.registerFunction("all_positions", Colonies.class.getDeclaredMethod("allPositions"));

        context.registerFunction("swap", SpelFunctionWrapper.class.getDeclaredMethod("swap", Object.class));
        context.registerFunction("farm", SpelFunctionWrapper.class.getDeclaredMethod("farm", Object.class));
        context.registerFunction("next", SpelFunctionWrapper.class.getDeclaredMethod("next", Object.class, String.class));
        context.registerFunction("prev", SpelFunctionWrapper.class.getDeclaredMethod("prev", Object.class, String.class));
        context.registerFunction("space", SpelFunctionWrapper.class.getDeclaredMethod("space", Object.class, String.class));

        context.registerFunction("swapStr", Base.class.getDeclaredMethod("swap", String.class));
        context.registerFunction("swapFunc", Base.class.getDeclaredMethod("swap", List.class));
        context.registerFunction("spaceStr", Base.class.getDeclaredMethod("space", String.class, String.class));
        context.registerFunction("spaceFunc", Base.class.getDeclaredMethod("space", List.class, String.class));

        context.registerFunction("cacheKey", Base.class.getDeclaredMethod("cacheKey", String.class));
        context.registerFunction("cache_key", Base.class.getDeclaredMethod("cacheKey", String.class));

        context.registerFunction("eventFleetFilter",
                EventFleetFunctions.class.getDeclaredMethod("filter", Map.class));
        context.registerFunction("event_fleet_filter",
                EventFleetFunctions.class.getDeclaredMethod("filter", Map.class));
        context.registerFunction("first", ListFunctions.class.getDeclaredMethod("first", List.class));
        context.registerFunction("endingPlanet",
                EventFleetFunctions.class.getDeclaredMethod("endingPlanet", EventFleet.class));
    }

    /**
     * Register condition-related functions.
     * These functions create condition objects from parameters.
     */
    private static void registerConditionFunctions(StandardEvaluationContext context) throws NoSuchMethodException {
        log.debug("Registering condition functions");

        // RESOURCE_COUNT: Check if planet has minimum resources
        // Usage: #hasResourcesCount({source: 'p[1:2:3]', count: '3m'})
        context.registerFunction("hasResourcesCount",
            ConditionFunctions.class.getDeclaredMethod("hasResourcesCount", java.util.Map.class));

        // RESOURCE: Check resource availability with optional leave amount
        // Usage: #hasResources({planet: 'source', have: 'm100 c50', leave: 'm10'})
        context.registerFunction("hasResources",
            ConditionFunctions.class.getDeclaredMethod("hasResources", java.util.Map.class));

        // SHIPS: Check ship availability
        // Usage: #areShips({planet: 'source', need: 'transporterSmall:5,battleship:3', leave: 'transporterSmall:2'})
        context.registerFunction("areShips",
            ConditionFunctions.class.getDeclaredMethod("areShips", java.util.Map.class));

        // FLEET_EXPIRED: Wait for fleet to expire
        // Usage: #isFleetExpired({source: 'source', missions: 'TRANSPORT|ATTACK', timeout: '1H'})
        context.registerFunction("isFleetExpired",
            ConditionFunctions.class.getDeclaredMethod("isFleetExpired", java.util.Map.class));

        // EXPR: Generic expression condition
        // Usage: #isExpressionTrue({term: 'fuel_level', op: 'eq', value: 'ready'})
        context.registerFunction("isExpressionTrue",
            ConditionFunctions.class.getDeclaredMethod("isExpressionTrue", java.util.Map.class));

        // TECH: Check technology level
        // Usage reserved for a future concrete HasTechnologyCondition implementation.
        context.registerFunction("hasTechnology",
            ConditionFunctions.class.getDeclaredMethod("hasTechnology", java.util.Map.class));

        // NO_MISSIONS: Check no missions on planet
        // Usage: #hasNoMissions({planet: 'source'}) or #hasNoMissions({source: 'source', missions: 'TRANSPORT|ATTACK'})
        context.registerFunction("hasNoMissions",
            ConditionFunctions.class.getDeclaredMethod("hasNoMissions", java.util.Map.class));

        // FLEET_SLOT: Check available fleet slots
        // Usage: #hasFleetSlots({required: '5'})
        context.registerFunction("hasFleetSlots",
            ConditionFunctions.class.getDeclaredMethod("hasFleetSlots", java.util.Map.class));

        // ATTACK_NOT_EXPIRED: Wait for attack to expire
        // Usage: #isAttackNotExpired({target: 'target', timeout: '1H'})
        context.registerFunction("isAttackNotExpired",
            ConditionFunctions.class.getDeclaredMethod("isAttackNotExpired", java.util.Map.class));

        // SPY_NOT_EXPIRED: Wait for spy operation to expire
        // Usage: #isSpyNotExpired({target: 'target', timeout: '1H'})
        context.registerFunction("isSpyNotExpired",
            ConditionFunctions.class.getDeclaredMethod("isSpyNotExpired", java.util.Map.class));

        // IS_MODIFIED_AFTER: Check if data was modified after timestamp
        // Usage: #isModifiedAfter({first: 'scanCacheKey', second: 'fleetCacheKey'})
        context.registerFunction("isModifiedAfter",
            ConditionFunctions.class.getDeclaredMethod("isModifiedAfter", java.util.Map.class));
    }

    /**
     * Register resource-related functions.
     * Placeholder for future resource functions.
     */
    private static void registerResourceFunctions(StandardEvaluationContext context) throws NoSuchMethodException {
        log.debug("Registering resource functions");
        // TODO: Register resource-related functions as needed
        // Example:
        // context.registerFunction("totalResources", ResourceFunctions.class.getDeclaredMethod("totalResources", String.class));
    }
}
