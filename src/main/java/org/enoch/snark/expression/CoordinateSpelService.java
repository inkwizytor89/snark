package org.enoch.snark.expression;

import com.google.common.collect.ArrayListMultimap;
import lombok.RequiredArgsConstructor;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.TargetRepository;
import org.enoch.snark.instance.model.expression.coordinate.*;
import org.enoch.snark.instance.model.to.PlanetData;
import org.enoch.snark.instance.service.PlanetDataService;
import org.springframework.context.annotation.Scope;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.model.types.Expression.ALL;
import static org.enoch.snark.instance.service.CoordinateExpressionService.*;
import static org.enoch.snark.instance.si.module.AbstractThread.PROCESSING_SUFFIX;

@RequiredArgsConstructor
@Component
@Scope("prototype")
@SuppressWarnings("FieldCanBeLocal")
public class CoordinateSpelService {

    private final CacheEntryRepository cacheEntryRepository;
    private final ColonyRepository colonyRepository;
    private final TargetRepository targetRepository;
    private final PlanetDataService planetDataService;

    private final ExpressionParser parser = new SpelExpressionParser();
    private static ArrayListMultimap<String, PlanetData> expresionCache = ArrayListMultimap.create();
    private static Map<String, LocalDateTime> cacheVariablesEntries =new HashMap<>();

    public List<PlanetData> cached(String expression) {
        return cached(expression, Map.of());
    }

    public List<PlanetData> cached(String expression, Map<String, Object> expressionContext) {
        if (expression == null) {
            return null;
        }
        String expressionHash = expressionToHash(expressionContext, expression);
        updateCache();
        if(!expresionCache.containsKey(expressionHash)) {
        List<PlanetData> evaluated =  nonCached(expression, expressionContext);
            expresionCache.putAll(expressionHash, evaluated);
            System.err.println("Expression \""+expressionHash+"\" evaluated and cached as: "+evaluated.stream()
                    .map(PlanetData::toString)
                    .collect(Collectors.joining(";")));
        }
        return expresionCache.get(expressionHash);
    }

    public List<PlanetData> nonCached(String expression) {
        return nonCached(expression, Map.of());
    }

    public List<PlanetData> nonCached(String expression, Map<String, Object> expressionContext) {
        if (expression == null) {
            return null;
        }
        if(expression.equals(PLANETS)) {
            expression = "#all_planets()";
            System.err.println("Expression \""+PLANETS+"\" replaced with \""+expression+"\"");
        }
        if(expression.equals(MOONS)) {
            expression = "#all_moons()";
            System.err.println("Expression \""+MOONS+"\" replaced with \""+expression+"\"");
        }
        if(expression.equals(ALL)) {
            expression = "#all_moons()";
            System.err.println("Expression \""+ALL+"\" replaced with \""+expression+"\"");
        }
        if(expression.isEmpty()) {
            expression = "#allPositionsFunc()";
            System.err.println("Expression is empty replaced with \""+expression+"\"");
        }
        if(expression.equals(SPACE)) {
            expression = "#spaceFunc(#source,0)";
            System.err.println("Expression \""+SPACE+"\" replaced with \""+expression+"\"");
        }
        if(expression.equals(SWAP)) {
            expression = "#swapFunc(#source)";
            System.err.println("Expression \""+SPACE+"\" replaced with \""+expression+"\"");
        }
        String expressionHash = expressionToHash(expressionContext, expression);
        if(expresionCache.containsKey(expressionHash)) return expresionCache.get(expressionHash);
        else return evaluate(expression, expressionContext);
    }

//    public List<PlanetData> from(String expression) {
//        return from(expression, Map.of());
//    }
//
//    public List<PlanetData> from(String expression, Map<String, Object> expressionContext) {
//        if (expression == null) {
//            return null;
//        }
//        if(expression.equals(PLANETS)) {
//            expression = "#planets()";
//            System.err.println("Expression \""+PLANETS+"\" replaced with \""+expression+"\"");
//        }
//        if(expression.equals(MOONS)) {
//            expression = "#moons()";
//            System.err.println("Expression \""+MOONS+"\" replaced with \""+expression+"\"");
//        }
//        if(expression.equals(ALL)) {
//            expression = "#moons()";
//            System.err.println("Expression \""+ALL+"\" replaced with \""+expression+"\"");
//        }
//        if(expression.isEmpty()) {
//            expression = "#allPositions()";
//            System.err.println("Expression is empty replaced with \""+expression+"\"");
//        }
//        if(expression.equals("space")) {
//            expression = "#space(#source,0)";
//            System.err.println("Expression \""+PLANETS+"\" replaced with \""+expression+"\"");
//        }
////        String expressionHash = expressionToHash(expressionContext, expression);
////        updateCache();
////        if(expressionHash.equals("#swarm_nest")) {
////            expresionCache.removeAll(expressionHash);
////            System.err.println("Expression \""+expressionHash+"\" is dynamic, cache cleared");
////        }
////        if(!expresionCache.containsKey(expressionHash)) {
//            List<PlanetData> evaluated = evaluate(expression, expressionContext);
////            expresionCache.putAll(expressionHash, evaluated);
////            System.err.println("Expression \""+expressionHash+"\" evaluated and cached as: "+evaluated.stream()
////                    .map(PlanetData::toString)
////                    .collect(Collectors.joining(";")));
////        }
////        return expresionCache.get(expressionHash);
//        return evaluated;
//    }

    private void updateCache() {
        cacheEntryRepository.findAll().stream()
                .filter(entry -> !entry.key.endsWith(PROCESSING_SUFFIX))
                .forEach(entry -> {
                    cacheVariablesEntries.putIfAbsent(entry.key, entry.updated);
                    if(cacheVariablesEntries.get(entry.key).isBefore(entry.updated)) {
                        cacheVariablesEntries.put(entry.key, entry.updated);
                        clearCache(entry.key);
                    }
                });
    }

    public List<PlanetData> evaluate(String expression, Map<String, Object> expressionContext) {
        try {
            EvaluationContext context = createEvaluationContext(expressionContext);
            Expression expr = parser.parseExpression(expression);
            Object result = expr.getValue(context);

            // Handle result based on type
            if (result instanceof String) {
                return planetDataService.fetchCoordinates((String) result);
            } else  if (result instanceof List<?> listResult) {
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

    private static String expressionToHash(Map<String, Object> expressionContext, String expression) {
        String expressionHash = expression;
        for(Map.Entry<String, Object> entry : expressionContext.entrySet())
            expressionHash = expressionHash.replaceAll(entry.getKey(), entry.getValue().toString());
        return expressionHash;
    }

    /**
     * Create evaluation context with all variables and functions
     */
    private EvaluationContext createEvaluationContext(Map<String, Object> expressionContext) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        cacheEntryRepository.findAll().forEach(entry -> context.setVariable(entry.key, convertToVariable(entry.value, expressionContext)));
        expressionContext.forEach((key, value) -> context.setVariable(key, convertToVariable(value, expressionContext)));

        Colonies.setRepository(colonyRepository);
        Colonies.setRepository(planetDataService);
        Base.setRepository(planetDataService, cacheEntryRepository);
        CycleTrip.setRepository(planetDataService);
        Swarn.setRepository(planetDataService);
        
        // Register function methods using a helper class to handle overloads properly
        try {
            // Use SpelFunctionRegistry to register functions with proper overload support
            SpelFunctionRegistry.registerFunctions(context);

        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to register SpEL functions", e);
        }

        return context;
    }

    private Object convertToVariable(Object value, Map<String, Object> expressionContext) {
        if(value instanceof String stringValue) {
            if(!stringValue.contains("#")) return value;
            return nonCached(stringValue, expressionContext);
        }
        return value;
    }

    public void clearCache() {
        System.err.println("Clearing expression cache with "+expresionCache.size()+" entries");
        expresionCache.clear();
    }

    public void clearCache(String phrase) {
        expresionCache.keySet().stream()
                .filter(key -> key.contains(phrase))
                .forEach(key -> {
                    System.err.println("Expression cache cleared for key \""+key+"\" containing phrase \""+phrase+"\": "+expresionCache.get(key).stream()
                            .map(PlanetData::toString)
                            .collect(Collectors.joining(";")));
                    expresionCache.removeAll(key);
                });
    }
}
