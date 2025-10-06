package org.enoch.snark.instance.model.to;


import lombok.Getter;

import java.util.List;

import static org.enoch.snark.instance.service.PlanetService.NEXT;
import static org.enoch.snark.instance.service.PlanetService.PREV;

@Getter
public class PlanetTerm {

    public static final String ACTION_SEPARATOR = "-";
    public static final int ACTION_INDEX = 0;
    public static final int PLANET_INDEX = 1;
    private final FleetContext context;

    private PlanetData planetData;
    private String action;

    public PlanetTerm(String expression) {
        this(expression, FleetContext.builder().build());
    }

    public PlanetTerm(String expression, FleetContext context) {
        this.context = context;
        if (containsActionSeparator(expression)) {
            parseActionPlanetData(expression);
        } else if (isSingleCoordinate(expression)) {
            planetData = new PlanetData(new Planet(expression));
        } else {
            setActionWithContext(expression, context);
        }
    }

    private void setActionWithContext(String expression, FleetContext context) {
        action = expression;
        planetData = context.getSource();
        if(NEXT.equals(action) || PREV.equals(action)) {
            if(context.getTrip() == null || context.getTrip().isEmpty()) {
                throw new IllegalStateException("PlanetTerm has no trip for action " + action);
            }
        }
    }

    private void parseActionPlanetData(String expression) {
        String[] s = expression.split(ACTION_SEPARATOR);
        action = s[ACTION_INDEX];
        planetData = new PlanetData(new Planet(s[PLANET_INDEX]));
    }

    private static boolean isSingleCoordinate(String expression) {
        return expression.contains("[") && expression.contains("]");
    }

    private static boolean containsActionSeparator(String expression) {
        return expression.contains(ACTION_SEPARATOR);
    }
}
