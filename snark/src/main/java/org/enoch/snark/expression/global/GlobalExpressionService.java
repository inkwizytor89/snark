package org.enoch.snark.expression.global;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.TargetRepository;
import org.enoch.snark.expression.function.coordinate.Base;
import org.enoch.snark.expression.function.coordinate.Colonies;
import org.enoch.snark.expression.function.coordinate.CycleTrip;
import org.enoch.snark.expression.function.coordinate.Swarn;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.to.PlanetData;
import org.enoch.snark.instance.service.PlanetDataService;
import org.springframework.context.annotation.Scope;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.enoch.snark.instance.service.CoordinateExpressionService.MOONS;
import static org.enoch.snark.instance.service.CoordinateExpressionService.PLANETS;
import static org.enoch.snark.instance.service.CoordinateExpressionService.SPACE;
import static org.enoch.snark.instance.service.CoordinateExpressionService.SWAP;

/**
 * Global expression evaluation service with TTL-based caching.
 * 
 * Provides unified SpEL parsing and evaluation.
 * Supports:
 * - TTL-based caching
 * - Function registration via {@link GlobalSpelFunctionRegistry}
 * - Expression evaluation with automatic caching
 * - Cache invalidation
 * 
 * Usage:
 * <pre>
 * List&lt;PlanetData&gt; coords = expressionService.evaluateCoordinates("planets");
 * 
 * List&lt;AbstractCondition&gt; conditions = expressionService.evaluate(
 *     "#hasResourcesCount({source: 'p[1:2:3]', count: '3m'})",
 *     Map.of(),
 *     List.class
 * );
 * </pre>
 */
@RequiredArgsConstructor
@Component
@Scope("prototype")
@Slf4j
public class GlobalExpressionService {

    private static final Pattern COORDINATE_LITERAL = Pattern.compile(
            "(?i)^[pmd]\\[\\s*\\d+\\s*:\\s*\\d+\\s*:\\s*\\d+\\s*\\](\\s*;\\s*[pmd]\\[\\s*\\d+\\s*:\\s*\\d+\\s*:\\s*\\d+\\s*\\])*$");

    private final ExpressionParser parser = new SpelExpressionParser();
    private final GlobalExpressionCache cache;

    private final CacheEntryRepository cacheEntryRepository;
    private final ColonyRepository colonyRepository;
    private final TargetRepository targetRepository;
    private final PlanetDataService planetDataService;
    
//    public GlobalExpressionService() {
//        this(new GlobalExpressionCache());
//    }
//
//    @Autowired
//    public GlobalExpressionService(GlobalExpressionCache cache) {
//        this.parser = new SpelExpressionParser();
//        this.cache = cache;
//    }

    /**
     * Evaluate a SpEL expression with caching.
     *
     * @param expression The SpEL expression to evaluate (e.g., "#all_planets()" or "#hasResourcesCount({...})")
     * @param context Variables and context available during evaluation
     * @param returnType Expected return type
     * @return Evaluated result, cached for 50 seconds
     * @throws IllegalStateException if expression evaluation fails
     */
    public <T> T evaluate(String expression, Map<String, Object> context, Class<T> returnType) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Expression cannot be null or empty");
        }

        String cacheKey = generateCacheKey(expression, context, returnType);

        if (cache.containsValid(cacheKey)) {
            GlobalExpressionCache.CacheEntry<?> entry = cache.get(cacheKey);
            if (entry != null) {
                log.debug("Cache hit for expression '{}'", expression);
                return returnType.cast(entry.value);
            }
        }

        // Evaluate and cache
        try {
            log.debug("Evaluating expression '{}'", expression);
            EvaluationContext spelContext = createEvaluationContext(context);
            Expression parsedExpr = parser.parseExpression(expression);
            T result = returnType.cast(parsedExpr.getValue(spelContext));

            // Store in cache
            cache.put(cacheKey, result, expression);
            log.debug("Expression '{}' evaluated and cached with TTL {}ms",
                expression, GlobalExpressionCache.CACHE_TTL.toMillis());

            return result;

        } catch (Exception e) {
            log.error("Failed to evaluate expression '{}'", expression, e);
            throw new IllegalStateException("Failed to evaluate SpEL expression: " + expression, e);
        }
    }

    /**
     * Evaluate without caching.
     *
     * @param expression The SpEL expression to evaluate
     * @param context Variables and context available during evaluation
     * @param returnType Expected return type
     * @return Evaluated result
     * @throws IllegalStateException if expression evaluation fails
     */
    public <T> T evaluateNoCached(String expression, Map<String, Object> context, Class<T> returnType) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Expression cannot be null or empty");
        }

        try {
            log.debug("Evaluating expression without cache: '{}'", expression);
            EvaluationContext spelContext = createEvaluationContext(context);
            Expression parsedExpr = parser.parseExpression(expression);
            return returnType.cast(parsedExpr.getValue(spelContext));

        } catch (Exception e) {
            log.error("Failed to evaluate expression (no cache): '{}'", expression, e);
            throw new IllegalStateException("Failed to evaluate SpEL expression: " + expression, e);
        }
    }

    /**
     * Evaluate a coordinate selector or SpEL expression and normalize it to database-backed coordinates.
     * Coordinate evaluation is uncached to preserve the dynamic behavior of fleet and colony selectors.
     */
    public List<PlanetData> evaluateCoordinates(String expression) {
        return evaluateCoordinates(expression, Map.of());
    }

    public List<PlanetData> evaluateCoordinates(String expression, Map<String, Object> context) {
        if (expression == null) {
            return null;
        }

        String normalizedExpression = switch (expression) {
            case "none" -> null;
            case PLANETS -> "#all_planets()";
            case MOONS -> "#all_moons()";
            case "all" -> "#all_moons()";
            case "" -> "#allPositionsFunc()";
            case SPACE -> "#spaceFunc(#source,0)";
            case SWAP -> "#swapFunc(#source)";
            default -> expression;
        };
        if (normalizedExpression == null) {
            return List.of();
        }
        if (COORDINATE_LITERAL.matcher(normalizedExpression).matches()) {
            return planetDataService.fetchCoordinates(normalizedExpression);
        }

        Object result = evaluateNoCached(normalizedExpression, context, Object.class);
        if (result instanceof String coordinates) {
            return planetDataService.fetchCoordinates(coordinates);
        }
        if (result instanceof PlanetData planetData) {
            return List.of(planetData);
        }
        if (result instanceof List<?> values) {
            List<PlanetData> coordinates = new ArrayList<>(values.size());
            for (Object value : values) {
                if (!(value instanceof PlanetData planetData)) {
                    throw new IllegalStateException("Coordinate expression '" + expression
                            + "' returned a list containing "
                            + (value == null ? "null" : value.getClass().getSimpleName())
                            + " instead of PlanetData");
                }
                coordinates.add(planetData);
            }
            return coordinates;
        }

        throw new IllegalStateException("Coordinate expression '" + expression + "' must return "
                + "String, PlanetData, or List<PlanetData>, got: "
                + (result == null ? "null" : result.getClass().getSimpleName()));
    }

    /**
     * Evaluate a condition configuration expression into concrete conditions.
     *
     * Supports a single condition expression or multiple conditions separated by a top-level pipe.
     * Pipes inside quoted values or nested structures are preserved.
     *
     * @param expression condition expression, e.g.
     *                   "#hasResourcesCount({source: 'source', count: '3m'}) | #areShips({planet: 'source', need: 'transporterSmall:5'})"
     * @param context evaluation context
     * @return parsed condition list
     */
    @SuppressWarnings("unchecked")
    public List<AbstractCondition> evaluateConditions(String expression, Map<String, Object> context) {
        if (expression == null || expression.isBlank() || "[]".equals(expression.trim())) {
            return List.of();
        }

        String trimmedExpression = expression.trim();
        if (trimmedExpression.startsWith("[")) {
            throw new IllegalStateException("JSON condition arrays are not supported by GlobalExpressionService. Use SpEL condition expressions instead: " + trimmedExpression);
        }

        String cacheKey = generateCacheKey(trimmedExpression, context, List.class);
        if (cache.containsValid(cacheKey)) {
            GlobalExpressionCache.CacheEntry<?> cachedEntry = cache.get(cacheKey);
            if (cachedEntry != null) {
                log.debug("Cache hit for condition list '{}'", trimmedExpression);
                return (List<AbstractCondition>) cachedEntry.value;
            }
        }

        List<String> conditionExpressions = splitTopLevelConditions(trimmedExpression).stream()
                .filter(part -> !part.isBlank())
                .map(String::trim)
                .toList();
        List<AbstractCondition> result = conditionExpressions.stream()
                .map(part -> evaluate(part, context, AbstractCondition.class))
                .toList();

        cache.put(cacheKey, result, conditionExpressions.toArray(String[]::new));
        return result;
    }

    /**
     * Invalidate all cache entries.
     */
    public void invalidateAll() {
        int size = cache.clearAll();
        log.info("Cleared {} cache entries", size);
    }

    /**
     * Invalidate all cache entries associated with a key.
     */
    public void invalidateByKey(String key) {
        int removedCount = cache.invalidateByKey(key);
        log.info("Invalidated {} cache entries for key '{}'", removedCount, key);
    }

    /**
     * Get current cache size (for monitoring/debugging).
     */
    public int getCacheSize() {
        return cache.getSize();
    }

    /**
     * Get cache statistics (for monitoring/debugging).
     */
    public String getCacheStats() {
        return String.format("Cache size: %d entries", cache.getSize());
    }

    /**
     * Generate cache key from expression and context.
     */
    private String generateCacheKey(String expression, Map<String, Object> context, Class<?> returnType) {
        String contextKey = context.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> encodeCacheKeyPart(entry.getKey())
                + encodeCacheKeyPart(entry.getValue() == null ? "null" : entry.getValue().getClass().getName())
                + encodeCacheKeyPart(String.valueOf(entry.getValue())))
            .reduce("", String::concat);

        return returnType.getName() + ":" + expression + ":context:" + contextKey;
    }

    private String encodeCacheKeyPart(String value) {
        return value.length() + ":" + value;
    }

    private List<String> splitTopLevelConditions(String expression) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int roundDepth = 0;
        int curlyDepth = 0;
        int squareDepth = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < expression.length(); i++) {
            char currentChar = expression.charAt(i);

            if (currentChar == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (currentChar == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            } else if (!inSingleQuote && !inDoubleQuote) {
                if (currentChar == '(') roundDepth++;
                else if (currentChar == ')') roundDepth--;
                else if (currentChar == '{') curlyDepth++;
                else if (currentChar == '}') curlyDepth--;
                else if (currentChar == '[') squareDepth++;
                else if (currentChar == ']') squareDepth--;
                else if (currentChar == '|' && roundDepth == 0 && curlyDepth == 0 && squareDepth == 0) {
                    parts.add(current.toString().trim());
                    current.setLength(0);
                    continue;
                }
            }

            current.append(currentChar);
        }

        if (!current.isEmpty()) {
            parts.add(current.toString().trim());
        }
        return parts;
    }

    /**
     * Create SpEL evaluation context with registered functions and variables.
     */
    private EvaluationContext createEvaluationContext(Map<String, Object> context) {
        StandardEvaluationContext spelContext = new StandardEvaluationContext();
        cacheEntryRepository.findAll().forEach(entry -> spelContext.setVariable(entry.key, convertToVariable(entry.value, context)));
        
        // Add all provided variables
        context.forEach((key, value) -> spelContext.setVariable(key, convertToVariable(value, context)));

        Colonies.setRepository(colonyRepository);
        Colonies.setRepository(planetDataService);
        Base.setRepository(planetDataService, cacheEntryRepository);
        CycleTrip.setRepository(planetDataService);
        Swarn.setRepository(planetDataService);

        // Register all SpEL functions
        try {
            GlobalSpelFunctionRegistry.registerFunctions(spelContext);
        } catch (NoSuchMethodException e) {
            log.error("Failed to register SpEL functions", e);
            throw new RuntimeException("Failed to register SpEL functions", e);
        }

        return spelContext;
    }

    private Object convertToVariable(Object value, Map<String, Object> expressionContext) {
        if(value instanceof String stringValue) {
            if(!stringValue.contains("#")) return value;
            return evaluate(stringValue, expressionContext, Object.class);
        }
        return value;
    }
}
