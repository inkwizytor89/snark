package org.enoch.snark.expression.global;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Condition function implementations for SpEL expressions.
 * 
 * These static methods are registered as SpEL functions via {@link GlobalSpelFunctionRegistry}.
 * Each method takes a Map of parameters and returns a condition object (or in this demo, a ConditionDto).
 * 
 * All parameters are passed as a Map with named keys for readability:
 * - Required parameters must be present or an exception is thrown
 * - Optional parameters have default values
 * 
 * Examples:
 * <pre>
 * #resourceCount({source: 'p[1:2:3]', count: '3m'})
 * #ships({planet: 'source', need: 'sg:5', leave: 'sg:2'})
 * #resource({planet: 'target', have: 'metal:100 crystal:50'})
 * #fleetExpired({source: 'source', missions: 'TRANSPORT|ATTACK', timeout: '1H'})
 * </pre>
 * 
 * Note: This is a demo implementation. In production, these would return actual AbstractCondition subclasses.
 */
@Slf4j
public class ConditionFunctions {

    /**
     * Create a RESOURCE_COUNT condition.
     * 
     * Checks if a planet has at least a minimum amount of resources.
     * 
     * Parameters:
     * - source (required): Planet identifier (e.g., 'source', 'target', 'p[1:2:3]')
     * - count (required): Resource count (e.g., '3m', '100k')
     * 
     * @param params The parameters map
     * @return A condition dto (demo version)
     * @throws IllegalArgumentException if required parameters are missing
     */
    public static ConditionDto resourceCount(Map<String, Object> params) {
        log.debug("Creating resourceCount condition with params: {}", ExpressionParsingUtils.mapToString(params));
        
        String source = ExpressionParsingUtils.getRequiredParam(params, "source");
        String count = ExpressionParsingUtils.getRequiredParam(params, "count");

        source = ExpressionParsingUtils.parsePlanet(source);

        return new ConditionDto("RESOURCE_COUNT")
            .param("source", source)
            .param("count", count);
    }

    /**
     * Create a RESOURCE condition.
     * 
     * Checks resource availability with optional leave amount.
     * 
     * Parameters:
     * - planet (required): Planet identifier
     * - have (required): Resources to check (e.g., 'metal:100 crystal:50')
     * - leave (optional): Resources to leave behind (default: empty)
     * 
     * @param params The parameters map
     * @return A condition dto
     * @throws IllegalArgumentException if required parameters are missing
     */
    public static ConditionDto resource(Map<String, Object> params) {
        log.debug("Creating resource condition with params: {}", ExpressionParsingUtils.mapToString(params));
        
        String planet = ExpressionParsingUtils.getRequiredParam(params, "planet");
        String have = ExpressionParsingUtils.getRequiredParam(params, "have");
        String leave = ExpressionParsingUtils.getOptionalParam(params, "leave", "");

        planet = ExpressionParsingUtils.parsePlanet(planet);
        Map<String, String> haveResources = ExpressionParsingUtils.parseResources(have);
        Map<String, String> leaveResources = ExpressionParsingUtils.parseResources(leave);

        return new ConditionDto("RESOURCE")
            .param("planet", planet)
            .param("have", haveResources)
            .param("leave", leaveResources);
    }

    /**
     * Create a SHIPS condition.
     * 
     * Checks ship availability with optional leave amount.
     * 
     * Parameters:
     * - planet (required): Planet identifier
     * - need (required): Ships needed (e.g., 'sg:5 bc:3')
     * - leave (optional): Ships to leave (default: empty)
     * 
     * @param params The parameters map
     * @return A condition dto
     * @throws IllegalArgumentException if required parameters are missing
     */
    public static ConditionDto ships(Map<String, Object> params) {
        log.debug("Creating ships condition with params: {}", ExpressionParsingUtils.mapToString(params));
        
        String planet = ExpressionParsingUtils.getRequiredParam(params, "planet");
        String need = ExpressionParsingUtils.getRequiredParam(params, "need");
        String leave = ExpressionParsingUtils.getOptionalParam(params, "leave", "");

        planet = ExpressionParsingUtils.parsePlanet(planet);
        Map<String, Integer> needShips = ExpressionParsingUtils.parseShipsMap(need);
        Map<String, Integer> leaveShips = ExpressionParsingUtils.parseShipsMap(leave);

        return new ConditionDto("SHIPS")
            .param("planet", planet)
            .param("need", needShips)
            .param("leave", leaveShips);
    }

    /**
     * Create a FLEET_EXPIRED condition.
     * 
     * Waits for fleet to expire (missions to complete).
     * 
     * Parameters:
     * - source (required): Planet identifier
     * - missions (required): Missions to wait for (e.g., 'TRANSPORT|ATTACK|ESPIONAGE')
     * - timeout (optional): How long to wait (default: '1H')
     * 
     * @param params The parameters map
     * @return A condition dto
     * @throws IllegalArgumentException if required parameters are missing
     */
    public static ConditionDto fleetExpired(Map<String, Object> params) {
        log.debug("Creating fleetExpired condition with params: {}", ExpressionParsingUtils.mapToString(params));
        
        String source = ExpressionParsingUtils.getRequiredParam(params, "source");
        String missions = ExpressionParsingUtils.getRequiredParam(params, "missions");
        String timeout = ExpressionParsingUtils.getOptionalParam(params, "timeout", "1H");

        source = ExpressionParsingUtils.parsePlanet(source);
        long timeoutMs = ExpressionParsingUtils.parseDurationToMillis(timeout);

        return new ConditionDto("FLEET_EXPIRED")
            .param("source", source)
            .param("missions", missions)
            .param("timeoutMs", timeoutMs);
    }

    /**
     * Create an EXPR (Expression) condition.
     * 
     * Generic expression condition for comparing cache values.
     * 
     * Parameters:
     * - term (required): Variable name in cache (e.g., 'fuel_level')
     * - op (required): Operator (e.g., 'eq', 'gt', 'lt')
     * - value (required): Expected value (e.g., 'ready', '5')
     * 
     * @param params The parameters map
     * @return A condition dto
     * @throws IllegalArgumentException if required parameters are missing
     */
    public static ConditionDto expr(Map<String, Object> params) {
        log.debug("Creating expr condition with params: {}", ExpressionParsingUtils.mapToString(params));
        
        String term = ExpressionParsingUtils.getRequiredParam(params, "term");
        String op = ExpressionParsingUtils.getRequiredParam(params, "op");
        String value = ExpressionParsingUtils.getRequiredParam(params, "value");

        return new ConditionDto("EXPR")
            .param("term", term)
            .param("operator", op)
            .param("value", value);
    }

    /**
     * Create a TECH (Technology) condition.
     * 
     * Checks if a planet has a technology at a minimum level.
     * 
     * Parameters:
     * - source (required): Planet identifier
     * - tech (required): Technology name (e.g., 'IMPULSE', 'COMBUSTION')
     * - level (required): Minimum technology level (1-15)
     * 
     * @param params The parameters map
     * @return A condition dto
     * @throws IllegalArgumentException if required parameters are missing
     */
    public static ConditionDto tech(Map<String, Object> params) {
        log.debug("Creating tech condition with params: {}", ExpressionParsingUtils.mapToString(params));
        
        String source = ExpressionParsingUtils.getRequiredParam(params, "source");
        String tech = ExpressionParsingUtils.getRequiredParam(params, "tech");
        String level = ExpressionParsingUtils.getRequiredParam(params, "level");

        source = ExpressionParsingUtils.parsePlanet(source);
        long levelNum = Long.parseLong(level);

        return new ConditionDto("TECH")
            .param("source", source)
            .param("technology", tech)
            .param("level", levelNum);
    }

    /**
     * Create a NO_MISSIONS condition.
     * 
     * Checks that there are no missions on a planet.
     * 
     * Parameters:
     * - planet (required): Planet identifier
     * 
     * @param params The parameters map
     * @return A condition dto
     * @throws IllegalArgumentException if required parameters are missing
     */
    public static ConditionDto noMissions(Map<String, Object> params) {
        log.debug("Creating noMissions condition with params: {}", ExpressionParsingUtils.mapToString(params));
        
        String planet = ExpressionParsingUtils.getRequiredParam(params, "planet");
        planet = ExpressionParsingUtils.parsePlanet(planet);

        return new ConditionDto("NO_MISSIONS")
            .param("planet", planet);
    }

    /**
     * Create a FLEET_SLOT condition.
     * 
     * Checks if there are enough available fleet slots.
     * 
     * Parameters:
     * - planet (required): Planet identifier
     * - required (required): Number of slots needed
     * 
     * @param params The parameters map
     * @return A condition dto
     * @throws IllegalArgumentException if required parameters are missing
     */
    public static ConditionDto fleetSlot(Map<String, Object> params) {
        log.debug("Creating fleetSlot condition with params: {}", ExpressionParsingUtils.mapToString(params));
        
        String planet = ExpressionParsingUtils.getRequiredParam(params, "planet");
        String required = ExpressionParsingUtils.getRequiredParam(params, "required");

        planet = ExpressionParsingUtils.parsePlanet(planet);
        int requiredSlots = Integer.parseInt(required);

        return new ConditionDto("FLEET_SLOT")
            .param("planet", planet)
            .param("required", requiredSlots);
    }

    /**
     * Create an ATTACK_NOT_EXPIRED condition.
     * 
     * Waits for an attack to expire.
     * 
     * Parameters:
     * - target (required): Target planet identifier
     * - timeout (optional): How long to wait (default: '1H')
     * 
     * @param params The parameters map
     * @return A condition dto
     * @throws IllegalArgumentException if required parameters are missing
     */
    public static ConditionDto attackNotExpired(Map<String, Object> params) {
        log.debug("Creating attackNotExpired condition with params: {}", ExpressionParsingUtils.mapToString(params));
        
        String target = ExpressionParsingUtils.getRequiredParam(params, "target");
        String timeout = ExpressionParsingUtils.getOptionalParam(params, "timeout", "1H");

        target = ExpressionParsingUtils.parsePlanet(target);
        long timeoutMs = ExpressionParsingUtils.parseDurationToMillis(timeout);

        return new ConditionDto("ATTACK_NOT_EXPIRED")
            .param("target", target)
            .param("timeoutMs", timeoutMs);
    }

    /**
     * Create a SPY_NOT_EXPIRED condition.
     * 
     * Waits for a spy operation to expire.
     * 
     * Parameters:
     * - target (required): Target planet identifier
     * - timeout (optional): How long to wait (default: '1H')
     * 
     * @param params The parameters map
     * @return A condition dto
     * @throws IllegalArgumentException if required parameters are missing
     */
    public static ConditionDto spyNotExpired(Map<String, Object> params) {
        log.debug("Creating spyNotExpired condition with params: {}", ExpressionParsingUtils.mapToString(params));
        
        String target = ExpressionParsingUtils.getRequiredParam(params, "target");
        String timeout = ExpressionParsingUtils.getOptionalParam(params, "timeout", "1H");

        target = ExpressionParsingUtils.parsePlanet(target);
        long timeoutMs = ExpressionParsingUtils.parseDurationToMillis(timeout);

        return new ConditionDto("SPY_NOT_EXPIRED")
            .param("target", target)
            .param("timeoutMs", timeoutMs);
    }

    /**
     * Create an IS_MODIFIED_AFTER condition.
     * 
     * Checks if data was modified after a certain time.
     * 
     * Parameters:
     * - target (required): Target planet identifier
     * - since (required): Time duration (e.g., '30M', '1H')
     * 
     * @param params The parameters map
     * @return A condition dto
     * @throws IllegalArgumentException if required parameters are missing
     */
    public static ConditionDto modifiedAfter(Map<String, Object> params) {
        log.debug("Creating modifiedAfter condition with params: {}", ExpressionParsingUtils.mapToString(params));
        
        String target = ExpressionParsingUtils.getRequiredParam(params, "target");
        String since = ExpressionParsingUtils.getRequiredParam(params, "since");

        target = ExpressionParsingUtils.parsePlanet(target);
        long sinceMs = ExpressionParsingUtils.parseDurationToMillis(since);

        return new ConditionDto("IS_MODIFIED_AFTER")
            .param("target", target)
            .param("sinceMs", sinceMs);
    }
}
