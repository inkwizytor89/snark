package org.enoch.snark.instance.model.action;

import org.enoch.snark.db.dao.CacheEntryDAO;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.to.Planet;

public class PlanetExpression {

    public static final String EXPRESSION_SEPARATOR = "-";
    public static final int ACTION_INDEX = 0;
    public static final int PLANET_INDEX = 1;
    public static final String SWAP = "swap";
    public static final String NEXT = "next";
    public static final String PREV = "prev";
    public static final String FLEET_TO = "fleet_to";
    public static final String FLEET_ON = "fleet_on";
    public static final String FLEET = "fleet";

    public static Planet from(String input) {
        if(input == null) throw new IllegalStateException("PlanetExpression can not parse "+ input);
        input = input.toLowerCase().trim();
        if (input.contains(EXPRESSION_SEPARATOR)) {
            String[] s = input.split(EXPRESSION_SEPARATOR);
            String action = s[ACTION_INDEX];
            Planet planet = new Planet(s[PLANET_INDEX]);

            if(action.contains(NEXT)) {
                ColonyEntity next = Instance.next(ColonyDAO.getInstance().get(planet));
                if(next!=null) return next.toPlanet();
                else return null;
            } else if(action.contains(PREV)) {
                ColonyEntity prev = Instance.prev(ColonyDAO.getInstance().get(planet));
                if(prev!=null) return prev.toPlanet();
                else return null;
            } else if(action.contains(SWAP)) {
                return planet.swapType();
            } else return null;
        } else if(CacheEntryDAO.getInstance().getCacheEntry(input) != null) {
            return new Planet(CacheEntryDAO.getInstance().getValue(input));
        }
        return new Planet(input);
    }

    public static String next(Planet planet) {
        return NEXT + EXPRESSION_SEPARATOR + planet;
    }

    public static String prev(Planet planet) {
        return PREV + EXPRESSION_SEPARATOR + planet;
    }

    public static String swap(Planet planet) {
        return SWAP + EXPRESSION_SEPARATOR + planet;
    }
}
