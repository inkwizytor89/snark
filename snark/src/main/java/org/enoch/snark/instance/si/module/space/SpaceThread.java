package org.enoch.snark.instance.si.module.space;

import com.google.common.collect.ArrayListMultimap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.snark.action.command.GalaxyAnalyzeCommand;
import org.enoch.common.DateUtil;
import org.enoch.common.NumberUtil;
import org.enoch.common.time.Duration;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.db.repository.ConfigRepository;
import org.enoch.snark.instance.model.to.*;
import org.enoch.snark.instance.model.uc.SystemUC;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.enoch.snark.instance.si.module.ThreadMap.*;

@RequiredArgsConstructor
public class SpaceThread extends AbstractThread {

    public static final String threadType = SpaceDefinition.THREAD_TYPE;
    protected static final Logger LOG = Logger.getLogger( SpaceThread.class.getName());
    private final ConfigRepository configRepository;
    private int threadPause = 300;
    private String spaceHash = StringUtils.EMPTY;

    private LocalDateTime lastCheck = LocalDateTime.now();
    private Duration expiredTime = new Duration("P7D");
    private SpaceDefinition config;

    @Override
    protected void onStart() {
        super.onStart();
        config = new SpaceDefinition();
        config.update(configRepository.findByThread_Module_NameAndThread_Name(
                threadEntity.module.name, "main"));
        config.update(threadEntity.configs);
    }

    @Override
    protected void onStep() {
        String newSpaceHash = generateNewSpaceHashIfNeeded();
        if (newSpaceHash == null) return;

        List<SystemViewRange> spacedRange = spaceRange();

        ArrayListMultimap<SystemView, Planet> spyCoordinate = spyCoordinate();
        buildGalaxyEntityMap(spacedRange).entrySet().stream()
                .filter(entry -> entry.getValue() == null || DateUtil.isExpired(entry.getValue().updated, expiredTime.getValue()))
                .sorted(Comparator.comparingInt(a -> a.getKey().galaxy * 1000 + a.getKey().system))
                .forEach(entry -> pushGalaxyAnalyzeCommandIfNeeded(entry, spyCoordinate));

        List<AbstractCommand> commandList = container.peek();
        logList("Pushed commands "+commandList.size(), commandList);
        spaceHash = newSpaceHash;
    }

    @Override
    public ThreadDefinition threadConfiguration(String thread, String module) {
        return config.threadConfiguration(threadEntity.name, threadEntity.module.name);
    }

    private void pushGalaxyAnalyzeCommandIfNeeded(Entry<SystemView, GalaxyEntity> entry, ArrayListMultimap<SystemView, Planet> spyCoordinate) {
        GalaxyAnalyzeCommand command = new GalaxyAnalyzeCommand(entry.getKey());

        command.setSpyPositions(spyCoordinate.get(entry.getKey()));
        if(isConfigured(config.getSpyPattern())) command.setSpyNew(config.getSpyPattern().toLowerCase());

        command.setSource(getSourceForCommand());
        // TODO: Use the configured spy pattern to find targets in newly added ranges without database records.
        // TODO: Include unknown planets in the selected range that have not yet been attacked.
        if(!spyCoordinate.isEmpty() && command.getSpyPositions().isEmpty() && entry.getValue() != null) {

//            log("skipping "+ entry.getKey()+" nothing to spy from "+SPY_COORDINATE+" = "+getNearestConfig(SPY_COORDINATE, "no value"));
        } else {
//            log(command.getSystemView()+" "+command.getSpyPositions().size());
            pushCommand(command);
        }
    }

    private ColonyEntity getSourceForCommand() {
        if(!isConfigured(config.getCoordinate())) return null;
        List<PlanetData> coordinate = expressionService.evaluateCoordinates(config.getCoordinate());
        if(coordinate.isEmpty()) return null;
        return colonyRepository.byPlanet(coordinate.getFirst().getPlanet());
    }

    private Map<SystemView, GalaxyEntity> buildGalaxyEntityMap(List<SystemViewRange> spacedRange) {
        Map<SystemView, GalaxyEntity> galaxyEntityMap = new HashMap<>();
        int systemMax = Objects.requireNonNullElse(config.getSystemMax(), 499);
        boolean wrap = Boolean.TRUE.equals(config.getWrapSystem());
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
        if(Boolean.parseBoolean(threadEntity.debug)) {
            String spyRangesString = spacedRange.stream().map(SystemViewRange::toString).collect(Collectors.joining(", "));
            long nullCount = galaxyEntityMap.values().stream().filter(Objects::isNull).count();
            log(spyRangesString + " - SystemView total "+  galaxyEntityMap.size()+" in that "+ nullCount + " never checked");
        }
        return galaxyEntityMap;
    }

    private String generateNewSpaceHashIfNeeded() {
        expiredTime.update(config.getExpiredTime());
        String newSpaceHash = spaceToProcess();
        if(!DateUtil.isExpired(lastCheck, expiredTime.getValue()) && spaceHash.equals(newSpaceHash)) return null;
        saveProcessingStatus(IN_PROGRESS_PROCESSING);
        container.create();
        lastCheck = LocalDateTime.now();
        log(newSpaceHash);

        // limit must be set before push
        limit = NumberUtil.toLong(threadEntity.commandLimit); // def10L
        return newSpaceHash;
    }

    private String spaceToProcess() {
        if(isConfigured(config.getCoordinate()) && isConfigured(config.getRange())) {
            Planet coordinate = expressionService.evaluateCoordinates(config.getCoordinate()).getFirst().getPlanet();
            return "range="+config.getRange()+
                    " coordinate="+ coordinate;
        }
        if(isConfigured(config.getRange())) return "range="+config.getRange();
        if(config.isGalaxyMaxConfigured()) return "for "+config.getGalaxyMax()+" galaxies";
        int galaxyMax = colonyRepository.findAll().stream()
                .filter(p -> p.galaxy != null)
                .mapToInt(p -> p.galaxy)
                .max()
                .orElse(0);
        return "for known "+galaxyMax+" galaxies";
    }

    private List<SystemViewRange> spaceRange() {
        List<SystemViewRange> ranges = new ArrayList<>();
        if(isConfigured(config.getCoordinate()) && isConfigured(config.getRange())) {
            Integer range = Integer.valueOf(config.getRange());
            List<Planet> coordinateList = expressionService.evaluateCoordinates(config.getCoordinate()).stream().map(PlanetData::getPlanet).toList();
            for(Planet planet : coordinateList) ranges.add(new SystemViewRange(planet.galaxy, new Range<>(planet.system - range, planet.system + range)));

        } else {
            if(isConfigured(config.getRange())) {
                ranges = SystemViewRange.parse(config.getRange());
            } else {
                int galaxyMax = config.isGalaxyMaxConfigured() ? Objects.requireNonNullElse(config.getGalaxyMax(), -1) : -1;
                if(galaxyMax == -1) galaxyMax = colonyRepository.findAll().stream()
                        .filter(p -> p.galaxy != null)
                        .mapToInt(p -> p.galaxy)
                        .max()
                        .orElse(0);

                int systemMax = Objects.requireNonNullElse(config.getSystemMax(), 499);
                for(int i= 1; i<=galaxyMax; i++) {
                    ranges.add(new SystemViewRange(i, new Range<>(1, systemMax)));
                }
            }
        }
        return ranges;
    }
    
    private ArrayListMultimap<SystemView, Planet> spyCoordinate() {
        if(!isConfigured(config.getCoordinate())) return ArrayListMultimap.create();

        List<PlanetData> nearestCoordinate = expressionService.evaluateCoordinates(config.getCoordinate());
        if(nearestCoordinate.size() > 1) throw new IllegalStateException(threadEntity.name+" required only one coordinate to use "+SPY_COORDINATE+"was "+config.getCoordinate()+" and found "+nearestCoordinate.size()+" elements: "+nearestCoordinate);

        PlanetData planetData = nearestCoordinate.getFirst();
        if(!isConfigured(config.getSpyCoordinate())) return ArrayListMultimap.create();
        String spyCoordinateString = config.getSpyCoordinate();

        Map<String, Object> stringObjectMap = Map.of("source", singletonList(planetData));
        List<Planet> spyCoordinate = expressionService.evaluateCoordinates(spyCoordinateString, stringObjectMap).stream().map(PlanetData::getPlanet).toList();
        ArrayListMultimap<SystemView, Planet> spyCoordinateMap = ArrayListMultimap.create();
        spyCoordinate.forEach(planet -> spyCoordinateMap.put(planet.getSystemView(), planet));
        return spyCoordinateMap;
    }

    private boolean isConfigured(String value) {
        return StringUtils.isNotBlank(value);
    }
}
