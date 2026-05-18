package org.enoch.snark.expression;

import org.enoch.snark.instance.model.expression.coordinate.Base;
import org.enoch.snark.instance.model.expression.coordinate.Colonies;
import org.enoch.snark.instance.model.expression.coordinate.CycleTrip;
import org.enoch.snark.instance.model.expression.coordinate.Swarn;
import org.enoch.snark.instance.model.to.PlanetData;

import java.util.List;

/**
 * Wrapper class that provides unified method signatures for SpEL expression compatibility.
 * Spring EL doesn't support Java method overloading, so this class wraps the actual
 * functions and provides single-signature methods that handle type checking internally.
 */
public class SpelFunctionWrapper {
    
    /**
     * Wrapper for planets function that accepts both String and List parameters.
     * Called as #planets(value) where value can be String or List.
     */
    public static List<?> planets(Object param) {
        if (param instanceof String) {
            return Colonies.planets((String) param);
        } else if (param instanceof List<?>) {
            return Colonies.planets((List<PlanetData>) param);
        }
        throw new IllegalArgumentException("planets() accepts String or List, got: " + 
            (param != null ? param.getClass().getSimpleName() : "null"));
    }
    
    /**
     * Wrapper for moons function that accepts both String and List parameters.
     * Called as #moons(value) where value can be String or List.
     */
    public static List<?> moons(Object param) {
        if (param instanceof String) {
            return Colonies.moons((String) param);
        } else if (param instanceof List<?>) {
            return Colonies.moons((List<PlanetData>) param);
        }
        throw new IllegalArgumentException("moons() accepts String or List, got: " + 
            (param != null ? param.getClass().getSimpleName() : "null"));
    }
    
    /**
     * Wrapper for swap function that accepts both String and List parameters.
     * Called as #swap(value) where value can be String or List.
     */
    public static List<?> swap(Object param) {
        if (param instanceof String) {
            return Base.swap((String) param);
        } else if (param instanceof List<?>) {
            return Base.swap((List<PlanetData>) param);
        }
        throw new IllegalArgumentException("swap() accepts String or List, got: " + 
            (param != null ? param.getClass().getSimpleName() : "null"));
    }
    
    /**
     * Wrapper for farm function that accepts both String and List parameters.
     * Called as #farm(value) where value can be String or List.
     */
    public static List<?> farm(Object param) {
        if (param instanceof String) {
            return Swarn.farm((String) param);
        } else if (param instanceof List<?>) {
            return Swarn.farm((List<PlanetData>) param);
        }
        throw new IllegalArgumentException("farm() accepts String or List, got: " + 
            (param != null ? param.getClass().getSimpleName() : "null"));
    }
    
    /**
     * Wrapper for next function that accepts both String and List as first parameter.
     * Called as #next(current, trip).
     */
    public static List<?> next(Object current, String trip) {
        if (current instanceof String) {
            return CycleTrip.next((String) current, trip);
        } else if (current instanceof List<?>) {
            return CycleTrip.next((List<PlanetData>) current, trip);
        }
        throw new IllegalArgumentException("next() accepts String or List as first parameter, got: " + 
            (current != null ? current.getClass().getSimpleName() : "null"));
    }
    
    /**
     * Wrapper for prev function that accepts both String and List as first parameter.
     * Called as #prev(current, trip).
     */
    public static List<?> prev(Object current, String trip) {
        if (current instanceof String) {
            return CycleTrip.prev((String) current, trip);
        } else if (current instanceof List<?>) {
            return CycleTrip.prev((List<PlanetData>) current, trip);
        }
        throw new IllegalArgumentException("prev() accepts String or List as first parameter, got: " + 
            (current != null ? current.getClass().getSimpleName() : "null"));
    }
    
    /**
     * Wrapper for space function that accepts both String and List as first parameter.
     * Called as #space(coordinate, moveSystem).
     */
    public static List<?> space(Object coordinate, String moveSystem) {
        if (coordinate instanceof String) {
            return Base.space((String) coordinate, moveSystem);
        } else if (coordinate instanceof List<?>) {
            return Base.space((List<PlanetData>) coordinate, moveSystem);
        }
        throw new IllegalArgumentException("space() accepts String or List as first parameter, got: " + 
            (coordinate != null ? coordinate.getClass().getSimpleName() : "null"));
    }
}
