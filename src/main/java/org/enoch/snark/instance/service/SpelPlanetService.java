package org.enoch.snark.instance.service;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.TargetRepository;
import org.enoch.snark.instance.model.expression.planet.Next;
import org.enoch.snark.instance.model.expression.planet.Prev;
import org.enoch.snark.instance.model.expression.planet.Space;
import org.enoch.snark.instance.model.expression.planet.Swap;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.PlanetData;
import org.springframework.context.annotation.Scope;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.EMPTY;

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
public class SpelPlanetService {

    private static final String PLANETS = "PLANETS";
    private static final String MOONS = "MOONS";
    private static final String ALL = "ALL";
    private static final String EACH_POSITION = EMPTY;
    private static final String NONE = "NONE";

    private final CacheEntryRepository cacheEntryRepository;
    private final ColonyRepository colonyRepository;
    private final TargetRepository targetRepository;

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * Evaluate SpEL expression and return list of PlanetData
     *
     * @param expression SpEL expression as string
     * @return List of PlanetData matching the expression
     */
    public List<Planet> evaluate(String expression) {
        if (expression == null) {
            return null;
        }

        try {
            EvaluationContext context = createEvaluationContext();
            Expression expr = parser.parseExpression(expression);
            Object result = expr.getValue(context);

            // Handle result based on type
            if (result instanceof List<?> listResult) {
                @SuppressWarnings("unchecked")
                List<Planet> typedList = (List<Planet>) listResult;
                return typedList;
            } else if (result instanceof Planet) {
                return List.of((Planet) result);
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
    private EvaluationContext createEvaluationContext() {
        StandardEvaluationContext context = new StandardEvaluationContext();

        // Register value variables
        context.setVariable(PLANETS, getPlanetsList());
        context.setVariable(MOONS, getMoonsList());
        context.setVariable(ALL, getAllList());
        context.setVariable(EACH_POSITION, getEachPositionList());
        context.setVariable(NONE, getNoneList());
        cacheEntryRepository.findAll().forEach(entry -> context.setVariable(entry.key, entry.value));

        System.err.println("Registered SpEL variables: " + context.toString());

        // Initialize helper functions with repository references so they can parse String args
        SpelPlanetFunctions.setRepositories(cacheEntryRepository, colonyRepository, targetRepository);

        // Register function methods (they accept Object so they handle List<PlanetData> and String)
        try {
            Method swapMethod = Swap.class.getDeclaredMethod("execute", String.class);
            Method spaceMethod = Space.class.getDeclaredMethod("execute", String.class);
            Method spaceSystemMethod = Space.class.getDeclaredMethod("execute", String.class, String.class);
            Method nextMethod = Next.class.getDeclaredMethod("execute", String.class, String.class);
            Method prevMethod = Prev.class.getDeclaredMethod("execute", String.class, String.class);

            context.registerFunction("swap", swapMethod);
            context.registerFunction("space", spaceMethod);
            context.registerFunction("space_system", spaceSystemMethod);
            context.registerFunction("next", nextMethod);
            context.registerFunction("prev", prevMethod);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to register SpEL functions", e);
        }

        return context;
    }

    /**
     * Get all planets from repository
     */
    private List<PlanetData> getPlanetsList() {
        return colonyRepository.findByCode(PLANETS).stream()
                .map(PlanetData::new)
                .toList();
    }

    /**
     * Get all moons from repository
     */
    private List<PlanetData> getMoonsList() {
        return colonyRepository.findByCode(MOONS).stream()
                .map(PlanetData::new)
                .toList();
    }

    /**
     * Get all planets and moons
     */
    private List<PlanetData> getAllList() {
        return colonyRepository.findByCode(ALL).stream()
                .map(PlanetData::new)
                .toList();
    }

    /**
     * Get planets at each position
     */
    private List<PlanetData> getEachPositionList() {
        return colonyRepository.findByCode(EACH_POSITION).stream()
                .map(PlanetData::new)
                .toList();
    }

    /**
     * Get empty list (none)
     */
    private List<PlanetData> getNoneList() {
        return new ArrayList<>();
    }
}
