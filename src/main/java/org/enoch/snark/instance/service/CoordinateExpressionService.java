package org.enoch.snark.instance.service;

import com.google.common.collect.ArrayListMultimap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.TargetRepository;
import org.enoch.snark.instance.model.action.find.TripFinder;
import org.enoch.snark.instance.model.exception.NoColonyException;
import org.enoch.snark.instance.model.to.*;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.service.coordinate.CoordinateSpelService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@RequiredArgsConstructor
@Component
@Scope("prototype")
@Deprecated
public class CoordinateExpressionService {


    public static final String PLANETS = "planets";
    public static final String MOONS = "moons";
    public static final String ALL = "all";
    public static final String EACH_POSITION = EMPTY;
    public static final String NONE = "none";

    public static final String SWAP = "swap";
    public static final String NEXT = "next";
    public static final String FIND = "find";
    public static final String PREV = "prev";
    public static final String SPACE = "space";
    public static final String MAIN_FLEET_TO = "main_fleet_to";
    public static final String MAIN_FLEET_ON = "main_fleet_on";
    public static final String MAIN_FLEET = "main_fleet";

    public static final String PROBE_SWAM = "probe_swam";

    public static final String FARM = "farm";
    public static final String UNKNOWN = "unknown";

    public static final String TERM_SEPARATOR = ";";
    public static final String ACTION_SEPARATOR = "-";
    public static final int ACTION_INDEX = 0;
    public static final int PLANET_INDEX = 1;

    private final CoordinateSpelService coordinateSpelService;
    private final CacheEntryRepository cacheEntryRepository;
    private final ColonyRepository colonyRepository;
    private final TargetRepository targetRepository;

    private static ArrayListMultimap<String, PlanetData> expresionCache = ArrayListMultimap.create();

//    public List<PlanetData> from(String expression) {
//        return from(expression, Map.of());
//    }
//
//    public List<PlanetData> from(String expression, Map<String, Object> expressionContext) {
////        if(expression!= null && !expression.contains("#")) {
////            return colonyRepository.fromColoniesList(expression).stream()
////                    .map(PlanetData::new)
////                    .toList();
////        }
//
//        String expressionHash = expressionToHash(expressionContext, expression);
//        if(!expresionCache.containsKey(expressionHash)) {
//            List<PlanetData> evaluated = coordinateSpelService.from(expression, expressionContext);
//            expresionCache.putAll(expressionHash, evaluated);
//            System.err.println("Expression \""+expression+"\" evaluated and cached as: "+evaluated.stream()
//                    .map(PlanetData::toString)
//                    .collect(Collectors.joining(";")));
//        }
//        return expresionCache.get(expressionHash);
//    }

    private static String expressionToHash(Map<String, Object> expressionContext, String expression) {
        String expressionHash = expression;
        for(Map.Entry<String, Object> entry : expressionContext.entrySet())
            expressionHash = expressionHash.replaceAll(entry.getKey(), entry.getValue().toString());
        return expressionHash;
    }

//    @Deprecated
//    public List<PlanetData> fromExpression(Expression expression) {
//        if(!expresionCache.containsKey(expression.getValue())) {
//            List<PlanetData> evaluated = spelPlanetService.evaluate(expression.getValue());
//            String expressionString = evaluated.stream()
//                    .map(Planet::toString)
//                    .collect(Collectors.joining(";"));
//            List<PlanetData> values = fromExpression(expressionString);
//            expresionCache.putAll(expression.getValue(), values);
//
//            System.err.println("Expression \""+expression.getValue()+"\" evaluated and cached as: "+values.stream()
//                    .map(PlanetData::toString)
//                    .collect(Collectors.joining(";")));
//        }
//        return expresionCache.get(expression.getValue());
//    }

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
            return singletonList(getPlanetData(planetTerm.getPlanetData()));
        } else if(List.of(ALL, MOONS, PLANETS, EACH_POSITION, NONE).contains(planetTerm.getAction())) {
            return colonyRepository.findByCode(planetTerm.getAction()).stream()
                    .map(PlanetData::new)
                    .collect(Collectors.toList());
        } else if(planetTerm.getAction().contains(FIND)) {
            throw new NotImplementedException(FIND+" not yet implemented");
//            return CustomFinder.find(planetTerm.getAction()).stream()
//                    .map(PlanetData::new).toList();
        } else if(planetTerm.getAction().contains(NEXT)) {
            return singletonList(TripFinder.next(planetTerm));
        } else if(planetTerm.getAction().contains(PREV)) {
            return singletonList(TripFinder.prev(planetTerm));
        } else if(planetTerm.getAction().contains(SWAP)) {
            Planet swapCoordinate = planetTerm.getPlanetData().swapType();
            try{
                return singletonList(new PlanetData(colonyRepository.byPlanet(swapCoordinate)));
            } catch (NoColonyException e) {
                System.err.println("PlanetService - no colony found on swap coordinate "+swapCoordinate);
                return new ArrayList<>();
            }
        } else if(planetTerm.getAction().contains(SPACE)) {
            Planet space = planetTerm.getPlanetData().toSpace();
            return singletonList(new PlanetData(new TargetEntity(space)));
        } else if(planetTerm.getAction().contains(UNKNOWN)) {
            return targetRepository.findAll().stream().filter(target -> target.lastSpiedOn == null).map(PlanetData::new).toList();
        } else if(planetTerm.getAction().contains(FARM)) {
//            throw new NotImplementedException(FARM + " expression not yet implemented");
            Planet source = context.getSource().getPlanet();
            source = source.is(ColonyType.MOON) ? source.swapType() : source;

            Planet finalSource = source;
            List<Planet> others = colonyRepository.findAll().stream()
                    .map(PlanetEntity::toPlanet)
                    .filter(colony -> colony.galaxy.equals(finalSource.galaxy))
                    .filter(colony -> colony.is(ColonyType.PLANET))
                    .filter(colony -> !colony.equals(finalSource))
                    .toList();
            return targetRepository.findFarmsCloserTo(source, others).stream().map(PlanetData::new).toList();
        } else {
            String value = cacheEntryRepository.getValue(planetTerm.getAction());
            if(value != null) return fromExpression(value);
            throw new IllegalStateException("\""+input+"\" can not be interpreted as expression term");
        }
    }

    @Deprecated
    public PlanetData getPlanetData(Planet planet) {
        if(planet.position ==16) {
            return new PlanetData(new TargetEntity(planet));
        }
        Optional<TargetEntity> targetEntity = targetRepository.find(planet);
        return targetEntity.map(PlanetData::new).orElseGet(() -> new PlanetData(colonyRepository.byPlanet(planet)));
    }

    public void clearCache() {
        System.err.println("Clearing expression cache with "+expresionCache.size()+" entries");
        expresionCache.clear();
    }

}
