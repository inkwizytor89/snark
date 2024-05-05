package org.enoch.snark.instance.model.action;

import org.enoch.snark.db.dao.CacheEntryDAO;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.to.Planet;

public class PlanetExpression {

    public static final String EXPRESSION_SEPARATOR = "-";
    public static final int ACTION_INDEX = 0;
    public static final int PLANET_INDEX = 1;
    public static final String SWAP = "swap";
    public static final String NEXT = "next";
    public static final String FLEET_DESTINATION = "fleet_destination";
    public static final String FLEET_LOCATION = "fleet_location";
    public static final String FLEET = "fleet";

    public static Planet from(String input) {
        if(input == null) throw new IllegalStateException("PlanetExpression can not parse "+ input);
        if (input.contains(EXPRESSION_SEPARATOR)) {
            String[] s = input.split(EXPRESSION_SEPARATOR);
            String action = s[ACTION_INDEX];
            Planet planet = new Planet(s[PLANET_INDEX]);

            if(action.contains(NEXT)) {
                return Instance.next(ColonyDAO.getInstance().get(planet)).toPlanet();
            } else if(action.contains(SWAP)) {
                return planet.swapType();
            } else return null;
        } else if(input.contains(FLEET_DESTINATION)) {
            return new Planet(CacheEntryDAO.getInstance().getValue(FLEET_DESTINATION));
        } else if(input.contains(FLEET_LOCATION)) {
            return new Planet(CacheEntryDAO.getInstance().getValue(FLEET_LOCATION));
        } else if(input.contains(FLEET)) {
            return new Planet(CacheEntryDAO.getInstance().getValue(FLEET));
        }
        return new Planet(input);
    }

    public static String next(Planet planet) {
        return NEXT + EXPRESSION_SEPARATOR + planet;
    }

    public static String swap(Planet planet) {
        return SWAP + EXPRESSION_SEPARATOR + planet;
    }
}
