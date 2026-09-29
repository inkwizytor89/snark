// ================================
// ABSTRACT FUNCTION BASE CLASS
// ================================

package org.enoch.snark.expression.function;


import org.enoch.snark.expression.definition.SpelFunctionDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractSpelFunction {

    private static final Map<String, AbstractSpelFunction> functionRegistry = new HashMap<>();

    /**
     * Provide the function definition (name, description, parameters)
     */
    public abstract SpelFunctionDefinition getDefinition();

    /**
     * Execute the function with provided arguments
     */
    public abstract Object execute(Map<String, Object> args);

    /**
     * Register a function in the global registry
     */
    protected static void register(AbstractSpelFunction function) {
        functionRegistry.put(function.getDefinition().name(), function);
    }

    /**
     * Get all function definitions
     */
    public static List<SpelFunctionDefinition> definitions() {
        return functionRegistry.values().stream()
                .map(AbstractSpelFunction::getDefinition)
                .toList();
    }

    /**
     * Get all functions
     */
    public static List<AbstractSpelFunction> getFunctions() {
        return new ArrayList<>(functionRegistry.values());
    }

    /**
     * Get a specific function by name
     */
    public static AbstractSpelFunction getFunction(String name) {
        return functionRegistry.get(name);
    }
}

