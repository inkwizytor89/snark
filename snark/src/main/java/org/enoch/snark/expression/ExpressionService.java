package org.enoch.snark.expression;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.expression.coordinate.SpelFunctionRegistry;
import org.enoch.snark.expression.definition.SpellType;
import org.enoch.snark.expression.function.CoordinateMap;
import org.enoch.snark.expression.function.CoordinateFilter;
import org.enoch.snark.instance.model.to.PlanetData;
import org.enoch.snark.instance.service.PlanetDataService;
import org.springframework.context.annotation.Scope;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * General purpose expression service that evaluates SpEL expressions.
 *
 * - Does NOT cache results or variables.
 * - Variables are taken exclusively from the provided Map<String,Object> expressionContext.
 * - Functions are registered using {@link SpelFunctionRegistry}.
 */
@RequiredArgsConstructor
@Component
@Scope("prototype")
@SuppressWarnings("FieldCanBeLocal")
public class ExpressionService {

    private final PlanetDataService planetDataService;
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * Evaluate expression using only variables from expressionContext. Returns raw result (Object).
     */
    public Object evaluate(String expression, Map<String, Object> expressionContext) {
        if (expression == null) return null;
        try {
            EvaluationContext context = createEvaluationContext(expressionContext);
            Expression expr = parser.parseExpression(expression);
            return expr.getValue(context);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to evaluate expression: \"" + expression + "\"", e);
        }
    }

    /**
     * Evaluate expression and validate returned type against expected SpellType.
     * If validation fails an IllegalArgumentException is thrown.
     */
    public Object evaluateAs(String expression, Map<String, Object> expressionContext, SpellType expected) {
        Object result = evaluate(expression, expressionContext);
        validateType(result, expected);
        return result;
    }

    private void validateType(Object result, SpellType expected) {
        if (expected == null) return;
        switch (expected) {
            case STRING -> {
                if (!(result instanceof String))
                    throw new IllegalArgumentException("Expected result of type STRING but was: " + typeName(result));
            }
            case COORDINATES -> {
                if (result == null) return; // allow null, caller may treat as empty
                if (result instanceof String) return; // coordinates as string allowed
                if (result instanceof PlanetData) return;
                if (result instanceof List<?>) return; // element types are not strictly enforced here
                throw new IllegalArgumentException("Expected COORDINATES (String, PlanetData or List) but was: " + typeName(result));
            }
            case VOID -> {
                if (result != null)
                    throw new IllegalArgumentException("Expected VOID (null) result but was: " + typeName(result));
            }
            case MISSION, SHIPS -> {
                // no strict validation here; leave to caller
            }
            default -> {
            }
        }
    }

    private String typeName(Object o) {
        return o == null ? "null" : o.getClass().getSimpleName();
    }

    /**
     * Create evaluation context that contains ONLY variables from expressionContext
     * and registers available SpEL functions.
     */
    private EvaluationContext createEvaluationContext(Map<String, Object> expressionContext) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        if (expressionContext != null) {
            expressionContext.forEach(context::setVariable);
        }
        registerFunction(context);
        return context;
    }

    private void registerFunction(StandardEvaluationContext context) {
        try {
            CoordinateFilter.setRepository(planetDataService);
            context.registerFunction(CoordinateFilter.FUNCTION_NAME, CoordinateFilter.class.getDeclaredMethod("filter"));
            context.registerFunction(CoordinateMap.FUNCTION_NAME, CoordinateMap.class.getDeclaredMethod("map", List.class, String.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}

