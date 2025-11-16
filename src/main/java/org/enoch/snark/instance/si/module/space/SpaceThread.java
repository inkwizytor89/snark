package org.enoch.snark.instance.si.module.space;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.action.command.GalaxyAnalyzeCommand;
import org.enoch.snark.common.DateUtil;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.instance.model.to.*;
import org.enoch.snark.instance.model.uc.SystemUC;
import org.enoch.snark.instance.service.PlanetService;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

import static org.enoch.snark.instance.si.module.ThreadMap.*;

@RequiredArgsConstructor
public class SpaceThread extends AbstractThread {

    public static final String threadType = "space";
    protected static final Logger LOG = Logger.getLogger( SpaceThread.class.getName());
    public static final int DATA_COUNT = 10;
    private int threadPause = 300;
    private String spaceHash = StringUtils.EMPTY;

    private LocalDateTime lastCheck = LocalDateTime.now();
    private Duration expiredTime = new Duration("7D");

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

//    @Override
//    protected void onStart() {
//        super.onStart();
//        int galaxyMax = map.getConfigInteger(GALAXY_MAX, 6);
//        int systemMax = map.getConfigInteger(SYSTEM_MAX, 499);
//        for(int i = 1 ; i <= galaxyMax; i++) {
//            if(isNecessaryGalaxyPersist(i))
//                galaxyRepository.persistGalaxyMap(i, systemMax);
//        }
//        notExplored.addAll(galaxyRepository.findByUpdatedIsNull());
//    }

    private boolean isNecessaryGalaxyPersist(final Integer galaxy) {
        return galaxyRepository.findByGalaxyAndSystem(galaxy, 1).isEmpty();
    }


    protected void onOldStep() {
        Integer pageSize = map.getConfigInteger(PAGE_SIZE, DATA_COUNT);
//        if (!consumer.notingToPool()) return;
        if(!notExplored.isEmpty()) {
            log("Never checked galaxy: "+notExplored.size()+" page="+pageSize);
            for (int i = 0; i < pageSize; i++) {
                GalaxyEntity poll = notExplored.poll();
                if(poll != null) {
                    pushCommand(new GalaxyAnalyzeCommand(poll));
                }
            }
            return;
        }
        if (galaxyToView.isEmpty()) {
            galaxyToView = galaxyRepository.findByUpdatedBeforeOrderByUpdatedAsc(LocalDateTime.now().minusDays(5));
        }
        if (galaxyToView.isEmpty()) {
            return;
        }
        log("Outdated galaxy: "+galaxyToView.size()+" page="+pageSize);
        List<GalaxyEntity> toView = new ArrayList<>();
        if(galaxyToView.size() <= pageSize) {
            toView.addAll(galaxyToView);
        } else {
            toView.addAll(galaxyToView.subList(0,pageSize));
        }

        toView.forEach(galaxyEntity -> galaxyToView.remove(galaxyEntity));
        toView.forEach(galaxy -> pushCommand(new GalaxyAnalyzeCommand(galaxy)));
    }

    @Override
    protected void onStep() {
        expiredTime.update(getNearestConfig(EXPIRED_TIME, "7D"));

        if(!DateUtil.isExpired(lastCheck, expiredTime.getValue()) && spaceHash.equals(spaceHash())) return;
        System.out.println(spaceHash());

        List<SystemViewRange> spacedRange = spaceRange();
        int systemMax = map.getConfigInteger(SYSTEM_MAX, 499);
        boolean wrap = map.getConfigBoolean("wrap_system", true);
        Map<SystemView, GalaxyEntity> galaxyEntityMap = new HashMap<>();
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

        if(!isNearestConfig(COMMAND_LIMIT)) limit=10L; // limit must be se before push command
// tu trzeba jakos te wymagania wsprzadz do skanowania albo zeby puste sie szybko odpalało

        List<Planet> spyCoordinate = new ArrayList<>();
        if(isNearestConfig(COORDINATE)) {
            List<PlanetData> nearestCoordinate = getNearestCoordinate(StringUtils.EMPTY);
            if(nearestCoordinate.size() > 1) throw new IllegalStateException(threadType+" required only one coordinate to use "+SPY_COORDINATE+" but was "+getNearestConfig(COORDINATE, StringUtils.EMPTY));
            spyCoordinate = spyCoordinate(nearestCoordinate.getFirst());
        }

        List<Planet> finalSpyCoordinate = spyCoordinate;
        galaxyEntityMap.entrySet().stream()
                .filter(entry -> entry.getValue() == null || DateUtil.isExpired(entry.getValue().updated, expiredTime.getValue()))
                .forEach(entry -> {
                    GalaxyAnalyzeCommand command = new GalaxyAnalyzeCommand(entry.getKey());
                    command.setSpyPositions(finalSpyCoordinate.stream()
                            .filter(planet -> entry.getKey().equals(planet.getSystemView())).toList());
                    if(!command.getSpyPositions().isEmpty() && isNearestConfig(COORDINATE)) {
                        List<PlanetData> coordinate = getNearestCoordinate(PlanetService.NONE);
                        if(!coordinate.isEmpty()) command.setSource(colonyRepository.byPlanet(coordinate.getFirst().getPlanet()));
                    }
                    pushCommand(command);
                });

        spaceHash = spaceHash();
    }

    private String spaceHash() {
        if(isNearestConfig(COORDINATE)) return "range="+getNearestConfig(RANGE, StringUtils.EMPTY)+
                " coordinate="+ getNearestConfig(COORDINATE, StringUtils.EMPTY);
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
        if(isNearestConfig(COORDINATE)) {
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
        return ranges;
    }
    
    private List<Planet> spyCoordinate(PlanetData planetData) {
        if(! isNearestConfig(SPY_COORDINATE)) return new ArrayList<>();
        String spyCoordinateString = getNearestConfig(SPY_COORDINATE, StringUtils.EMPTY);

        FleetContext fleetContext = FleetContext.builder().source(planetData).build();
        return planetService.fromExpression(spyCoordinateString, fleetContext).stream().map(PlanetData::getPlanet).toList();
    }
}
