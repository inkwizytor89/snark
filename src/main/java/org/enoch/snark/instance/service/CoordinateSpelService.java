package org.enoch.snark.instance.service;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.TargetRepository;
import org.enoch.snark.instance.model.expression.coordinate.*;
import org.enoch.snark.instance.model.to.PlanetData;
import org.springframework.context.annotation.Scope;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Alternative implementation using Spring Expression Language (SpEL).
 * <p>
 * Values (accessible as variables):
 * - ALL, MOONS, PLANETS, EACH_POSITION, NONE - return List<PlanetData>
 * - PlanetData objects
 * <p>
 * Functions (operate on List<PlanetData> or String input which will be parsed to List<PlanetData>):
 * - next(Object) - accepts List<PlanetData> or String
 * - prev(Object) - accepts List<PlanetData> or String
 * - swap(Object) - accepts List<PlanetData>, PlanetData or String
 * <p>
 * Example expressions:
 * - "ALL"
 * - "next(PLANETS)"
 * - "swap(PLANETS.get(0))"
 */
@RequiredArgsConstructor
@Component
@Scope("prototype")
@SuppressWarnings("FieldCanBeLocal")
public class CoordinateSpelService {

    private final CacheEntryRepository cacheEntryRepository;
    private final ColonyRepository colonyRepository;
    private final TargetRepository targetRepository;

    private final ExpressionParser parser = new SpelExpressionParser();

    public List<PlanetData> evaluate(String expression) {
        return evaluate(expression, Map.of());
    }

    public List<PlanetData> evaluate(String expression, Map<String, Object> expressionContext) {
        if (expression == null) {
            return null;
        }

        try {
            EvaluationContext context = createEvaluationContext(expressionContext);
            Expression expr = parser.parseExpression(expression);
            Object result = expr.getValue(context);

            // Handle result based on type
            if (result instanceof List<?> listResult) {
                @SuppressWarnings("unchecked")
                List<PlanetData> typedList = (List<PlanetData>) listResult;
                return typedList;
            } else if (result instanceof PlanetData) {
                return List.of((PlanetData) result);
            } else {
                String resultType = result != null ? result.getClass().getSimpleName() : "null";
                throw new IllegalArgumentException("Expression must return List<PlanetData> or PlanetData, got: " + resultType);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to evaluate expression: \"" + expression + "\"", e);
        }
    }

    /**
     * Create evaluation context with all variables and functions
     */
    private EvaluationContext createEvaluationContext(Map<String, Object> expressionContext) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        cacheEntryRepository.findAll().forEach(entry -> {
            System.err.println("Registering SpEL variable: " + entry.key + " = " + entry.value);
            context.setVariable(entry.key, entry.value);
        });
        context.setVariables(expressionContext);

        Colonies.setRepository(colonyRepository);
        Base.setRepository(colonyRepository);
        CycleTrip.setRepository(colonyRepository);
        // Register function methods (they accept Object so they handle List<PlanetData> and String)
        try {

            Method testStringMethod = Test.class.getDeclaredMethod("test", String.class);
            Method testPlanetMethod = Test.class.getDeclaredMethod("test", String.class);
            context.registerFunction("test", testStringMethod);
            context.registerFunction("test", testPlanetMethod);

            context.registerFunction("planets", Colonies.class.getDeclaredMethod("planets"));
            context.registerFunction("moons", Colonies.class.getDeclaredMethod("moons"));
            context.registerFunction("all", Colonies.class.getDeclaredMethod("all"));
            context.registerFunction("all_positions", Colonies.class.getDeclaredMethod("allPositions"));
            context.registerFunction("none", Colonies.class.getDeclaredMethod("none"));

            context.registerFunction("swap", Base.class.getDeclaredMethod("swap", String.class));
            context.registerFunction("swap", Base.class.getDeclaredMethod("swap", List.class));
            context.registerFunction("space", Base.class.getDeclaredMethod("space", String.class, String.class));
            context.registerFunction("space", Base.class.getDeclaredMethod("space", List.class, String.class));

            context.registerFunction("next", CycleTrip.class.getDeclaredMethod("next", String.class, String.class));
            context.registerFunction("next", CycleTrip.class.getDeclaredMethod("next", List.class, String.class));
            context.registerFunction("prev", CycleTrip.class.getDeclaredMethod("prev", String.class, String.class));
            context.registerFunction("prev", CycleTrip.class.getDeclaredMethod("prev", List.class, String.class));

        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to register SpEL functions", e);
        }

        return context;
    }
}
