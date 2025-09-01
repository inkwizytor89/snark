package org.enoch.snark.instance.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.instance.model.action.find.CustomFinder;
import org.enoch.snark.instance.model.action.find.TripFinder;
import org.enoch.snark.instance.model.to.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isBlank;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class PlanetService {

    public static final String SWAP = "swap";
    public static final String NEXT = "next";
    public static final String FIND = "find";
    public static final String PREV = "prev";
    public static final String MAIN_FLEET_TO = "main_fleet_to";
    public static final String MAIN_FLEET_ON = "main_fleet_on";
    public static final String MAIN_FLEET = "main_fleet";

    public static final String PROBE_SWAM = "probe_swam";

    public static final String FARM = "farm";

    public static final String TERM_SEPARATOR = ";";
    public static final String ACTION_SEPARATOR = "-";
    public static final int ACTION_INDEX = 0;
    public static final int PLANET_INDEX = 1;

    private final CacheEntryRepository cacheEntryRepository;

    public List<PlanetData> fromFleetPlan(FleetPlan fleetPlan) {
        return fromExpression(fleetPlan.getTarget(), FleetContext.fromFleetPlan(fleetPlan));
    }

    public List<PlanetData> fromExpression(String expression) {
        return fromExpression(expression, FleetContext.builder().build());
    }

    public List<PlanetData> fromExpression(String expression, FleetContext context) {
        List<PlanetData> planetDataList = new ArrayList<>();
        if(expression == null) return null;
        String[] termExpression = expression.split(TERM_SEPARATOR);
        for(String term : termExpression) {
            planetDataList.addAll(termExpression(term, context));
        }
        return planetDataList;
    }

    private List<PlanetData> termExpression(String input, FleetContext context) {
        if(input == null) return null;
        input = input.toLowerCase().trim();
        PlanetTerm planetTerm = new PlanetTerm(input, context);

            if(planetTerm.getAction() == null) {
                return singletonList(planetTerm.getPlanetData());
            } else if(planetTerm.getAction().contains(FIND)) {
                return CustomFinder.find(planetTerm.getAction()).stream()
                        .map(PlanetData::new).toList();
            } else if(planetTerm.getAction().contains(NEXT)) {
                return singletonList(TripFinder.next(planetTerm));
            } else if(planetTerm.getAction().contains(PREV)) {
                return singletonList(TripFinder.prev(planetTerm));
            } else if(planetTerm.getAction().contains(SWAP)) {
                return singletonList(new PlanetData(planetTerm.getPlanetData().getPlanet().swapType()));
            } else if(planetTerm.getAction().contains(FARM)) {
                throw new NotImplementedException(FARM + " expression not yet implemented");
//                return FarmFinder.find(colony);
            } else {
                String value = cacheEntryRepository.getValue(planetTerm.getAction());
                if(value != null) return fromExpression(value);
                throw new IllegalStateException(planetTerm+" can not be interpreted as expression term");
            }
    }
}
