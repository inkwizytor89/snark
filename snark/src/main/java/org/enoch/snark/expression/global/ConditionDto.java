package org.enoch.snark.expression.global;

import java.util.HashMap;
import java.util.Map;

/**
 * Demo DTO representing a condition for testing purposes.
 * 
 * In production, this would be replaced with actual AbstractCondition subclasses.
 * This is useful for testing and demonstrating the GlobalExpressionService without
 * being tied to the actual condition implementations.
 */
public class ConditionDto {
    private final String type;
    private final Map<String, Object> parameters;

    /**
     * Create a new condition DTO.
     *
     * @param type The condition type (e.g., "RESOURCE_COUNT", "SHIPS")
     */
    public ConditionDto(String type) {
        this.type = type;
        this.parameters = new HashMap<>();
    }

    /**
     * Add a parameter to this condition.
     *
     * @param key Parameter name
     * @param value Parameter value
     * @return This condition (for method chaining)
     */
    public ConditionDto param(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }

    /**
     * Get the condition type.
     */
    public String getType() {
        return type;
    }

    /**
     * Get all parameters.
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Get a specific parameter value.
     *
     * @param key The parameter name
     * @return The parameter value, or null if not set
     */
    public Object getParameter(String key) {
        return parameters.get(key);
    }

    @Override
    public String toString() {
        return "ConditionDto{" +
                "type='" + type + '\'' +
                ", parameters=" + parameters +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConditionDto that = (ConditionDto) o;

        if (!type.equals(that.type)) return false;
        return parameters.equals(that.parameters);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + parameters.hashCode();
        return result;
    }
}
