package org.enoch.snark.expression.coordinate;

import org.enoch.snark.expression.global.GlobalSpelFunctionRegistry;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Compatibility entry point for the general expression service.
 */
public class SpelFunctionRegistry {

    public static void registerFunctions(StandardEvaluationContext context) throws NoSuchMethodException {
        GlobalSpelFunctionRegistry.registerCoordinateFunctions(context);
    }
}
