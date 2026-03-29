package org.enoch.snark.instance.model.to;


import lombok.Getter;

import static org.enoch.snark.instance.service.CoordinateExpressionService.*;

@Getter
public class PlanetTerm {

    public static final String ACTION_SEPARATOR = "-";
    public static final int ACTION_INDEX = 0;
    public static final int PLANET_INDEX = 1;
    private final FleetContext context;

    private Planet planetData;
    private String action;

    public PlanetTerm(String expression, FleetContext context) {
        this.context = context;
        if (containsActionSeparator(expression)) {
            parseActionPlanetData(expression);
        } else if (isSingleCoordinate(expression)) {
            planetData = new Planet(expression);
        } else {
            setActionWithContext(expression, context);
        }
    }

    private void setActionWithContext(String expression, FleetContext context) {
        action = expression;
        if(NEXT.equals(action) || PREV.equals(action)) {
            planetData = context.getSource().getPlanet();
            if(context.getTrip() == null || context.getTrip().isEmpty()) {
                throw new IllegalStateException("PlanetTerm has no trip for action " + action);
            }
        }
        if(SPACE.equals(action)) {
            planetData = context.getSource().getPlanet();
        }
        if(SWAP.equals(action)) {
            planetData = context.getSource().getPlanet();
        }
    }

    private void parseActionPlanetData(String expression) {
        String[] s = expression.split(ACTION_SEPARATOR);
        action = s[ACTION_INDEX];
        planetData = new Planet(s[PLANET_INDEX]);
    }

    private static boolean isSingleCoordinate(String expression) {
        return expression.contains("[") && expression.contains("]");
    }

    private static boolean containsActionSeparator(String expression) {
        return expression.contains(ACTION_SEPARATOR);
    }
}
