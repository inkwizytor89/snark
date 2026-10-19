package org.enoch.snark.expression.global;

import lombok.extern.slf4j.Slf4j;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
 * List&lt;PlanetData&gt; coords = expressionService.evaluate(
 *     "#all_planets()",
 *     Map.of(),
 *     List.class,
 *     Duration.ofMinutes(10)
 * );
 * 
 * List&lt;AbstractCondition&gt; conditions = expressionService.evaluate(
 *     "#hasResourcesCount({source: 'p[1:2:3]', count: '3m'})",
 *     Map.of(),
 *     List.class,
 *     Duration.ofMinutes(1)
 * );
 * </pre>
 */
@Component
@Slf4j
public class GlobalExpressionService {

    private final ExpressionParser parser;
    private final Map<String, CacheEntry<?>> cache;
    
    // Default TTL values for expression caching
    public static final Duration DEFAULT_TTL = Duration.ofMinutes(10);
    public static final Duration SHORT_TTL = Duration.ofMinutes(1);

    /**
     * Internal cache entry wrapper with TTL support.
     */
    static class CacheEntry<T> {
        final T value;
        final long createdAtMs;
        final long ttlMs;

        CacheEntry(T value, long ttlMs) {
            this.value = value;
            this.ttlMs = ttlMs;
            this.createdAtMs = System.currentTimeMillis();
        }

        /**
         * Check if this cache entry has expired.
         */
        boolean isExpired() {
            long elapsedMs = System.currentTimeMillis() - createdAtMs;
            return elapsedMs > ttlMs;
        }

        /**
         * Get remaining time until expiration in milliseconds.
         */
        long remainingTtlMs() {
            long elapsedMs = System.currentTimeMillis() - createdAtMs;
            return Math.max(0, ttlMs - elapsedMs);
        }
    }

    public GlobalExpressionService() {
        this.parser = new SpelExpressionParser();
        this.cache = new ConcurrentHashMap<>();
    }

    /**
     * Evaluate a SpEL expression with caching.
     *
     * @param expression The SpEL expression to evaluate (e.g., "#all_planets()" or "#hasResourcesCount({...})")
     * @param context Variables and context available during evaluation
     * @param returnType Expected return type
     * @param ttl Time-to-live for cache entry
     * @return Evaluated result, cached for the given TTL
     * @throws IllegalStateException if expression evaluation fails
     */
    public <T> T evaluate(String expression, Map<String, Object> context, Class<T> returnType, Duration ttl) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Expression cannot be null or empty");
        }

        String cacheKey = generateCacheKey(expression, context, returnType);

        // Check if cached entry exists and is not expired
        if (cache.containsKey(cacheKey)) {
            CacheEntry<?> entry = cache.get(cacheKey);
            if (!entry.isExpired()) {
                log.debug("Cache hit for expression '{}' (remaining TTL: {}ms)",
                    expression, entry.remainingTtlMs());
                return (T) entry.value;
            } else {
                // Remove expired entry
                cache.remove(cacheKey);
                log.debug("Expired cache entry removed for '{}'", expression);
            }
        }

        // Evaluate and cache
        try {
            log.debug("Evaluating expression '{}'", expression);
            EvaluationContext spelContext = createEvaluationContext(context);
            Expression parsedExpr = parser.parseExpression(expression);
            T result = (T) parsedExpr.getValue(spelContext);

            // Store in cache
            CacheEntry<T> entry = new CacheEntry<>(result, ttl.toMillis());
            cache.put(cacheKey, entry);
            log.debug("Expression '{}' evaluated and cached with TTL {}ms",
                expression, ttl.toMillis());

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
            return (T) parsedExpr.getValue(spelContext);

        } catch (Exception e) {
            log.error("Failed to evaluate expression (no cache): '{}'", expression, e);
            throw new IllegalStateException("Failed to evaluate SpEL expression: " + expression, e);
        }
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
    public List<AbstractCondition> evaluateConditions(String expression, Map<String, Object> context) {
        return evaluateConditions(expression, context, SHORT_TTL);
    }

    /**
     * Evaluate a condition configuration expression into concrete conditions with configurable cache settings.
     */
    @SuppressWarnings("unchecked")
    public List<AbstractCondition> evaluateConditions(String expression, Map<String, Object> context, Duration ttl) {
        if (expression == null || expression.isBlank() || "[]".equals(expression.trim())) {
            return List.of();
        }

        String trimmedExpression = expression.trim();
        if (trimmedExpression.startsWith("[")) {
            throw new IllegalStateException("JSON condition arrays are not supported by GlobalExpressionService. Use SpEL condition expressions instead: " + trimmedExpression);
        }

        String cacheKey = generateCacheKey(trimmedExpression, context, List.class);
        CacheEntry<?> cachedEntry = cache.get(cacheKey);
        if (cachedEntry != null && !cachedEntry.isExpired()) {
            log.debug("Cache hit for condition list '{}'", trimmedExpression);
            return (List<AbstractCondition>) cachedEntry.value;
        }
        if (cachedEntry != null) {
            cache.remove(cacheKey);
        }

        List<AbstractCondition> result = splitTopLevelConditions(trimmedExpression).stream()
                .filter(part -> !part.isBlank())
                .map(part -> evaluate(part.trim(), context, AbstractCondition.class, ttl))
                .toList();

        cache.put(cacheKey, new CacheEntry<>(result, ttl.toMillis()));
        return result;
    }

    /**
     * Invalidate all cache entries.
     */
    public void invalidateAll() {
        int size = cache.size();
        cache.clear();
        log.info("Cleared {} cache entries", size);
    }

    /**
     * Get current cache size (for monitoring/debugging).
     */
    public int getCacheSize() {
        return cache.size();
    }

    /**
     * Get cache statistics (for monitoring/debugging).
     */
    public String getCacheStats() {
        return String.format("Cache size: %d entries", cache.size());
    }

    /**
     * Generate cache key from expression and context.
     */
    private String generateCacheKey(String expression, Map<String, Object> context, Class<?> returnType) {
        // Include the return type so values with different shapes do not share a cache entry.
        String contextStr = context.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> e.getKey() + "=" + e.getValue())
            .reduce((a, b) -> a + ";" + b)
            .orElse("");

        return returnType.getName() + ":" + expression + ":" + contextStr.hashCode();
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
        
        // Add all provided variables
        context.forEach((key, value) -> spelContext.setVariable(key, value));

        // Register all SpEL functions
        try {
            GlobalSpelFunctionRegistry.registerFunctions(spelContext);
        } catch (NoSuchMethodException e) {
            log.error("Failed to register SpEL functions", e);
            throw new RuntimeException("Failed to register SpEL functions", e);
        }

        return spelContext;
    }
}
