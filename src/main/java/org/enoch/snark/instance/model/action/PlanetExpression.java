package org.enoch.snark.instance.model.action;

import org.enoch.snark.db.dao.CacheEntryDAO;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.model.action.find.CustomFinder;
import org.enoch.snark.instance.model.action.find.FarmFinder;
import org.enoch.snark.instance.model.action.find.TripFinder;
import org.enoch.snark.instance.model.to.Planet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.enoch.snark.instance.si.module.ThreadMap.ARRAY_SEPARATOR;

public class PlanetExpression {

    public static final String PLANET = "planet";
    public static final String MOON = "moon";

    public static final String SWAP = "swap";
    public static final String NEXT = "next";
    public static final String FIND = "find";
    public static final String PREV = "prev";
    public static final String MAIN_FLEET_TO = "main_fleet_to";
    public static final String MAIN_FLEET_ON = "main_fleet_on";
    public static final String MAIN_FLEET = "main_fleet";

    public static final String PROBE_SWAM = "probe_swam";

    public static final String FARM = "farm";

    public static final String EXPRESSION_SEPARATOR = "-";
    public static final int ACTION_INDEX = 0;
    public static final int PLANET_INDEX = 1;

    public static final List<String> HOSTILE = new ArrayList<>();

    public static List<ColonyEntity> toColonies(String input) {
        if(input == null) return new ArrayList<>();
        String code = input.trim().toLowerCase();
        if(MOON.equals(code) || PLANET.equals(code) || EMPTY.equals(code))
            return ColonyDAO.getInstance().getColonies(code);
        List<ColonyEntity> colonies = new ArrayList<>();
        Arrays.stream(code.split(ARRAY_SEPARATOR))
                .map(PlanetExpression::toColony)
                .forEach(colonies::addAll);
        return colonies;
    }

    public static List<ColonyEntity> toColony(String input) {
        if (input == null) return null;
        String code = input.trim().toLowerCase();
        if(HOSTILE.contains(code)) throw new IllegalStateException(code + "can not be mapped to ColonyEntity because ist hostile");
        List<ColonyEntity> colonies = new ArrayList<>();
        Objects.requireNonNull(toPlanetList(code)).stream()
                .map(planet -> ColonyDAO.getInstance().find(planet))
                .forEach(colonies::add);
        return colonies;
    }

    public static List<Planet> toPlanetList(String input) {
        if(input == null) return null;
        input = input.toLowerCase().trim();
        if (input.contains(EXPRESSION_SEPARATOR)) {
            String[] s = input.split(EXPRESSION_SEPARATOR);
            String action = s[ACTION_INDEX];
            Planet planet = new Planet(s[PLANET_INDEX]);
            ColonyEntity colony = ColonyDAO.getInstance().find(planet);


            if(action.contains(FIND)) {
                return CustomFinder.find(action, colony);
            } else if(action.contains(NEXT)) {
                ColonyEntity next = TripFinder.next(colony);
                return next!=null ? singletonList(next.toPlanet()) : null;
            } else if(action.contains(PREV)) {
                ColonyEntity prev = TripFinder.prev(colony);
                return prev!=null ? singletonList(prev.toPlanet()) : null;
            } else if(action.contains(SWAP)) {
                return singletonList(planet.swapType());
            } else if(action.contains(FARM)) {
                return FarmFinder.find(colony);
            } else return null;
        } else if(CacheEntryDAO.getInstance().getCacheEntry(input) != null) {
            return singletonList(Planet.parse(CacheEntryDAO.getInstance().getValue(input)));
        }
        return singletonList(new Planet(input));
    }

    public static String asExpression(ColonyEntity colony, String marker) {
        if(marker == null && colony.cpm != null) return makeAction(SWAP, colony.toPlanet());
        else if(marker == null) return null;
        else if(marker.contains(NEXT)) return makeAction(NEXT, colony.toPlanet());
        else if(marker.contains(PREV)) return makeAction(PREV, colony.toPlanet());
        else if(marker.contains(FARM)) return makeAction(FARM, colony.toPlanet());
        return marker;
    }

    private static String makeAction(String action, Planet planet) {
        return action + EXPRESSION_SEPARATOR + planet;
    }
}
