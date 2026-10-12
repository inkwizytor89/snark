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
 * Provides unified SpEL parsing and evaluation for different domains (coordinates, conditions, resources, etc).
 * Supports:
 * - TTL-based cache with domain-specific TTL values
 * - Function registration via {@link GlobalSpelFunctionRegistry}
 * - Expression evaluation with automatic caching
 * - Cache invalidation by domain
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
    
    // Default TTL values for different domains
    public static final Duration DEFAULT_TTL = Duration.ofMinutes(10);
    public static final Duration SHORT_TTL = Duration.ofMinutes(1);

    /**
     * Internal cache entry wrapper with TTL support.
     */
    static class CacheEntry<T> {
        final T value;
        final long createdAtMs;
        final long ttlMs;
        final String domain;

        CacheEntry(T value, long ttlMs, String domain) {
            this.value = value;
            this.ttlMs = ttlMs;
            this.domain = domain;
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
        return evaluate(expression, context, returnType, ttl, "default");
    }

    /**
     * Evaluate a SpEL expression with caching in a specific domain.
     *
     * @param expression The SpEL expression to evaluate
     * @param context Variables and context available during evaluation
     * @param returnType Expected return type
     * @param ttl Time-to-live for cache entry
     * @param domain Domain name for cache invalidation (e.g., "coordinates", "conditions", "resources")
     * @return Evaluated result, cached for the given TTL
     * @throws IllegalStateException if expression evaluation fails
     */
    public <T> T evaluate(String expression, Map<String, Object> context, Class<T> returnType, Duration ttl, String domain) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Expression cannot be null or empty");
        }

        String cacheKey = generateCacheKey(domain, expression, context);

        // Check if cached entry exists and is not expired
        if (cache.containsKey(cacheKey)) {
            CacheEntry<?> entry = cache.get(cacheKey);
            if (!entry.isExpired()) {
                log.debug("Cache hit for expression '{}' in domain '{}' (remaining TTL: {}ms)", 
                    expression, domain, entry.remainingTtlMs());
                return (T) entry.value;
            } else {
                // Remove expired entry
                cache.remove(cacheKey);
                log.debug("Expired cache entry removed for '{}' in domain '{}'", expression, domain);
            }
        }

        // Evaluate and cache
        try {
            log.debug("Evaluating expression '{}' in domain '{}'", expression, domain);
            EvaluationContext spelContext = createEvaluationContext(context);
            Expression parsedExpr = parser.parseExpression(expression);
            T result = (T) parsedExpr.getValue(spelContext);

            // Store in cache
            CacheEntry<T> entry = new CacheEntry<>(result, ttl.toMillis(), domain);
            cache.put(cacheKey, entry);
            log.debug("Expression '{}' evaluated and cached in domain '{}' with TTL {}ms", 
                expression, domain, ttl.toMillis());

            return result;

        } catch (Exception e) {
            log.error("Failed to evaluate expression '{}' in domain '{}'", expression, domain, e);
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
        return evaluateConditions(expression, context, SHORT_TTL, "conditions");
    }

    /**
     * Evaluate a condition configuration expression into concrete conditions with configurable cache settings.
     */
    @SuppressWarnings("unchecked")
    public List<AbstractCondition> evaluateConditions(String expression, Map<String, Object> context, Duration ttl, String domain) {
        if (expression == null || expression.isBlank() || "[]".equals(expression.trim())) {
            return List.of();
        }

        String trimmedExpression = expression.trim();
        if (trimmedExpression.startsWith("[")) {
            throw new IllegalStateException("JSON condition arrays are not supported by GlobalExpressionService. Use SpEL condition expressions instead: " + trimmedExpression);
        }

        String cacheKey = generateCacheKey(domain + ":list", trimmedExpression, context);
        CacheEntry<?> cachedEntry = cache.get(cacheKey);
        if (cachedEntry != null && !cachedEntry.isExpired()) {
            log.debug("Cache hit for condition list '{}' in domain '{}'", trimmedExpression, domain);
            return (List<AbstractCondition>) cachedEntry.value;
        }
        if (cachedEntry != null) {
            cache.remove(cacheKey);
        }

        List<AbstractCondition> result = splitTopLevelConditions(trimmedExpression).stream()
                .filter(part -> !part.isBlank())
                .map(part -> evaluate(part.trim(), context, AbstractCondition.class, ttl, domain + ":item"))
                .toList();

        cache.put(cacheKey, new CacheEntry<>(result, ttl.toMillis(), domain + ":list"));
        return result;
    }

    /**
     * Invalidate all cache entries for a specific domain.
     *
     * @param domain The domain to invalidate (e.g., "coordinates", "conditions")
     */
    public void invalidateByDomain(String domain) {
        int removedCount = (int) cache.entrySet().stream()
            .filter(e -> domain.equals(e.getValue().domain))
            .peek(e -> cache.remove(e.getKey()))
            .count();
        
        log.info("Invalidated {} cache entries for domain '{}'", removedCount, domain);
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
        return String.format("Cache size: %d entries, domains: %s",
            cache.size(),
            cache.values().stream()
                .map(e -> e.domain)
                .distinct()
                .toList()
        );
    }

    /**
     * Generate cache key from expression and context.
     */
    private String generateCacheKey(String domain, String expression, Map<String, Object> context) {
        // Create a deterministic hash from domain, expression, and context
        String contextStr = context.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> e.getKey() + "=" + e.getValue())
            .reduce((a, b) -> a + ";" + b)
            .orElse("");

        return domain + ":" + expression + ":" + contextStr.hashCode();
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
