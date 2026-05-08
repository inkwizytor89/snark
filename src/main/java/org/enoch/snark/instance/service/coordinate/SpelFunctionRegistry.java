package org.enoch.snark.instance.service.coordinate;

import org.enoch.snark.instance.model.expression.coordinate.*;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;
import java.util.Map;

/**
 * Helper class to register SpEL functions with proper handling for method overloading.
 * Since Spring EL doesn't support Java-style method overloading, we use wrapper methods
 * that accept Object parameters and determine the correct method to call based on type.
 */
public class SpelFunctionRegistry {

    public static void registerFunctions(StandardEvaluationContext context) throws NoSuchMethodException {
        
        // Test functions
        context.registerFunction("test", Test.class.getDeclaredMethod("test", String.class));
        
        // ============ PLANETS FUNCTIONS ============
        // No-arg version with new name (used in CoordinateSpelService rewrites)
        context.registerFunction("all_planets", Colonies.class.getDeclaredMethod("planets"));
        // Wrapper for String and List variants
        context.registerFunction("planets", SpelFunctionWrapper.class.getDeclaredMethod("planets", Object.class));
        
        // ============ MOONS FUNCTIONS ============
        // No-arg version with new name (used in CoordinateSpelService rewrites)
        context.registerFunction("all_moons", Colonies.class.getDeclaredMethod("moons"));
        // Wrapper for String and List variants
        context.registerFunction("moons", SpelFunctionWrapper.class.getDeclaredMethod("moons", Object.class));
        
        // ============ OTHER COLONIES FUNCTIONS ============
        context.registerFunction("all", Colonies.class.getDeclaredMethod("all"));
        context.registerFunction("none", Colonies.class.getDeclaredMethod("none"));
        context.registerFunction("allPositionsFunc", Colonies.class.getDeclaredMethod("allPositions"));
        context.registerFunction("all_positions", Colonies.class.getDeclaredMethod("allPositions"));
        
        // ============ WRAPPER FUNCTIONS FOR BACKWARD COMPATIBILITY ============
        // These are used for expressions in .properties files that use the original function names
        context.registerFunction("swap", SpelFunctionWrapper.class.getDeclaredMethod("swap", Object.class));
        context.registerFunction("farm", SpelFunctionWrapper.class.getDeclaredMethod("farm", Object.class));
        context.registerFunction("next", SpelFunctionWrapper.class.getDeclaredMethod("next", Object.class, String.class));
        context.registerFunction("prev", SpelFunctionWrapper.class.getDeclaredMethod("prev", Object.class, String.class));
        context.registerFunction("space", SpelFunctionWrapper.class.getDeclaredMethod("space", Object.class, String.class));
        
        // Alternative names for variants (for CoordinateSpelService rewrites)
        context.registerFunction("swapStr", Base.class.getDeclaredMethod("swap", String.class));
        context.registerFunction("swapFunc", Base.class.getDeclaredMethod("swap", List.class));
        context.registerFunction("spaceStr", Base.class.getDeclaredMethod("space", String.class, String.class));
        context.registerFunction("spaceFunc", Base.class.getDeclaredMethod("space", List.class, String.class));
        
        // ============ CACHE KEY FUNCTION ============
        context.registerFunction("cacheKey", Base.class.getDeclaredMethod("cacheKey", String.class));
        context.registerFunction("cache_key", Base.class.getDeclaredMethod("cacheKey", String.class));

        // ============ EVENT FILTER ============
        context.registerFunction("eventFilter", EventFleetFilter.class.getDeclaredMethod("filterField", List.class, String.class, Map.class));
        context.registerFunction("event_filter", EventFleetFilter.class.getDeclaredMethod("filterField", List.class, String.class, Map.class));
    }
}












