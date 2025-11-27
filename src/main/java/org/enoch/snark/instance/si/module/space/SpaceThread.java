package org.enoch.snark.instance.si.module.space;

import com.google.common.collect.ArrayListMultimap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.action.command.GalaxyAnalyzeCommand;
import org.enoch.snark.common.DateUtil;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.instance.model.to.*;
import org.enoch.snark.instance.model.uc.SystemUC;
import org.enoch.snark.instance.service.PlanetService;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.si.module.ThreadMap.*;

@RequiredArgsConstructor
public class SpaceThread extends AbstractThread {

    public static final String threadType = "space";
    protected static final Logger LOG = Logger.getLogger( SpaceThread.class.getName());
    public static final int DATA_COUNT = 10;
    private int threadPause = 300;
    private String spaceHash = StringUtils.EMPTY;

    private LocalDateTime lastCheck = LocalDateTime.now();
    private Duration expiredTime = new Duration("P7D");

    private final Queue<GalaxyEntity> notExplored = new PriorityQueue<>(
            Comparator.comparingInt(value -> value.galaxy*1000 + value.system)
    );
    private List<GalaxyEntity> galaxyToView = new ArrayList<>();


    @Override
    protected String getThreadType() {
        return threadType;
    }

    @Override
    protected String defaultPause() {
        return threadPause+"S";
    }

    @Override
    protected void onStep() {
        String newSpaceHash = generateNewSpaceHashIfNeeded();
        if (newSpaceHash == null) return;

        List<SystemViewRange> spacedRange = spaceRange();

        List<Planet> spyCoordinate = spyCoordinate();
        buildGalaxyEntityMap(spacedRange).entrySet().stream()
                .filter(entry -> entry.getValue() == null || DateUtil.isExpired(entry.getValue().updated, expiredTime.getValue()))
                .sorted(Comparator.comparingInt(a -> a.getKey().galaxy * 1000 + a.getKey().system))
                .forEach(entry -> pushGalaxyAnalyzeCommandIfNeeded(entry, spyCoordinate));

        spaceHash = newSpaceHash;
    }

    private void pushGalaxyAnalyzeCommandIfNeeded(Entry<SystemView, GalaxyEntity> entry, List<Planet> spyCoordinate) {
        GalaxyAnalyzeCommand command = new GalaxyAnalyzeCommand(entry.getKey());

        command.setSpyPositions(getSpyPositionList(entry, spyCoordinate));
        if(isNearestConfig(SPY_PATTERN)) command.setSpyNew(getNearestConfig(SPY_PATTERN, StringUtils.EMPTY).toLowerCase());

        command.setSource(getSourceForCommand());
// trzeba dorobic przypadek kiedy zwieksza sie zakres i nie mamy entity w bazie danych a dla niego nie ma farmy jeszcze bo nigdy nie bylo sknowane ale wtedy z patternu powinny zostac wyluskane
        // a co jak nie wyskanowalo jednej planety w ukladzie i jest ciagle niewiadomoa -farma powinna rowniez wybierac do skanowania te clee ktore sa z zakresu a jeszcze nie byly atakowane
        if(!spyCoordinate.isEmpty() && command.getSpyPositions().isEmpty() && entry.getValue() != null) {
            log("skipping "+ entry.getKey()+" nothing to spy from "+SPY_COORDINATE+" = "+getNearestConfig(SPY_COORDINATE, "no value"));
        } else pushCommand(command);
    }

    private ColonyEntity getSourceForCommand() {
        if(!isNearestConfig(COORDINATE)) return null;
        List<PlanetData> coordinate = getNearestCoordinate(PlanetService.NONE);
        if(coordinate.isEmpty()) return null;
        return colonyRepository.byPlanet(coordinate.getFirst().getPlanet());
    }

    private List<Planet> getSpyPositionList(Entry<SystemView, GalaxyEntity> entry, List<Planet> spyCoordinate) {
        List<Planet> spyPositionList = spyCoordinate.stream()
                .filter(planet -> entry.getKey().equals(planet.getSystemView())).toList();
        log(entry.getKey()+" "+spyPositionList.size());
        return spyPositionList;
    }

    private Map<SystemView, GalaxyEntity> buildGalaxyEntityMap(List<SystemViewRange> spacedRange) {
        Map<SystemView, GalaxyEntity> galaxyEntityMap = new HashMap<>();
        int systemMax = map.getConfigInteger(SYSTEM_MAX, 499);
        boolean wrap = map.getConfigBoolean(WRAP_SYSTEM, true);
        for(SystemViewRange range : spacedRange) {
            List<SystemView> systemList = SystemUC.range(range.getGalaxy(), range.systemRange.getFrom(), range.systemRange.getTo(), systemMax, wrap);
            systemList.forEach(systemView -> galaxyEntityMap.put(systemView, null));
        }
        for(SystemViewRange range : spacedRange) {
            List<GalaxyEntity> galaxyList = galaxyRepository.findRange(
                    range.getGalaxy(),
                    range.getSystemRange().getFrom(),
                    range.getSystemRange().getTo(),
                    systemMax,
                    wrap);
            galaxyList.forEach(galaxyEntity -> galaxyEntityMap.put(galaxyEntity.toSystemView(), galaxyEntity));
        }
        return galaxyEntityMap;
    }

    private String generateNewSpaceHashIfNeeded() {
        expiredTime.update(getNearestConfig(EXPIRED_TIME, "P7D"));
        String newSpaceHash = spaceToProcess();
        if(!DateUtil.isExpired(lastCheck, expiredTime.getValue()) && spaceHash.equals(newSpaceHash)) return null;
        commandsMap = ArrayListMultimap.create(); // clear old state
        lastCheck = LocalDateTime.now();
        log(newSpaceHash);

        // limit must be set before push
        if(!isNearestConfig(COMMAND_LIMIT)) limit= map().getConfigLong(COMMAND_LIMIT, 10L);
        return newSpaceHash;
    }

    private String spaceToProcess() {
        if(isNearestConfig(COORDINATE) && isNearestConfig(RANGE)) {
            Planet coordinate = getNearestCoordinate(StringUtils.EMPTY).getFirst().getPlanet();
            return "range="+getNearestConfig(RANGE, StringUtils.EMPTY)+
                    " coordinate="+ coordinate;
        }
        if(isNearestConfig(RANGE)) return "range="+getNearestConfig(RANGE, StringUtils.EMPTY);
        if(map().containsKey(GALAXY_MAX)) return "for "+map.getConfigInteger(GALAXY_MAX, -1)+" galaxies";
        int galaxyMax = colonyRepository.findAll().stream()
                .filter(p -> p.galaxy != null)
                .mapToInt(p -> p.galaxy)
                .max()
                .orElse(0);
        return "for known "+galaxyMax+" galaxies";
    }

    private List<SystemViewRange> spaceRange() {
        List<SystemViewRange> ranges = new ArrayList<>();
        if(isNearestConfig(COORDINATE) && isNearestConfig(RANGE)) {
            String coordinateString = getNearestConfig(COORDINATE, StringUtils.EMPTY);
            String rangeString = getNearestConfig(RANGE, StringUtils.EMPTY);
            Integer range = Integer.parseInt(rangeString);
            List<Planet> coordinateList = planetService.fromExpression(coordinateString).stream().map(PlanetData::getPlanet).toList();
            for(Planet planet : coordinateList) ranges.add(new SystemViewRange(planet.galaxy, new Range<>(planet.system - range, planet.system + range)));

        } else {
            if(isNearestConfig(RANGE)) {
                String systemViewRangeString = getNearestConfig(RANGE, StringUtils.EMPTY);
                ranges = SystemViewRange.parse(systemViewRangeString);
            } else {
                int galaxyMax = map.getConfigInteger(GALAXY_MAX, -1);
                if(galaxyMax == -1) galaxyMax = colonyRepository.findAll().stream()
                        .filter(p -> p.galaxy != null)
                        .mapToInt(p -> p.galaxy)
                        .max()
                        .orElse(0);

                int systemMax = map.getConfigInteger(SYSTEM_MAX, 499);
                for(int i= 1; i<=galaxyMax; i++) {
                    ranges.add(new SystemViewRange(i, new Range<>(1, systemMax)));
                }
            }
        }

        log("spacedRange "+ ranges.size()+": "+ranges.stream().map(SystemViewRange::toString).collect(Collectors.joining(", ")));
        return ranges;
    }
    
    private List<Planet> spyCoordinate() {
        if(!isNearestConfig(COORDINATE)) return new ArrayList<>();

        List<PlanetData> nearestCoordinate = getNearestCoordinate(StringUtils.EMPTY);
        if(nearestCoordinate.size() > 1) throw new IllegalStateException(map().name()+" required only one coordinate to use "+SPY_COORDINATE+" but was "+getNearestConfig(COORDINATE, StringUtils.EMPTY));

        PlanetData planetData = nearestCoordinate.getFirst();
        if(!isNearestConfig(SPY_COORDINATE)) return new ArrayList<>();
        String spyCoordinateString = getNearestConfig(SPY_COORDINATE, StringUtils.EMPTY);

        FleetContext fleetContext = FleetContext.builder().source(planetData).build();
        return planetService.fromExpression(spyCoordinateString, fleetContext).stream().map(PlanetData::getPlanet).toList();
    }
}
