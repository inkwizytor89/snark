package org.enoch.snark.module.farm;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.*;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.instance.BaseSI;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.ColonyPlaner;
import org.enoch.snark.model.service.MessageService;
import org.enoch.snark.module.AbstractThread;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.config.Config.MAIN;

public class FarmThread extends AbstractThread {

    public static final String threadName = "farm";
    public static final String INDEX_COUNT_CONFIG = "index_count";
    public static final String SLOT_CONFIG = "slot";
    public static final String EXPLORATION_AREA_CONFIG = "exploration_area";
    public static final String SPY_CACHE_CODE = "farm.spy_requests_code";
    public static final String WAR_CACHE_CODE = "farm.war_requests_code";
    public static final String FARM_INDEX_CACHE = "farm.index";
    public static final int FARM_SPY_SCALE = 2;
    public static final int SHORT_PAUSE = 4;
    public static final int LONG_PAUSE = 300;

    private CacheEntryEntity spyCacheEntry;
    private CacheEntryEntity warCacheEntry;
    private List<TargetEntity> spyWave = new LinkedList<>();
    private List<TargetEntity> attackWave = new LinkedList<>();
    private Integer slotToUse;
    private Integer propertiesSlotToUse;
    private Integer explorationArea;
    private ColonyPlaner planer;

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return pause;
    }

    @Override
    public int getRequestedFleetCount() {
        return 0;
    }

    @Override
    protected void onStart() {
        super.onStart();
        spyCacheEntry = cacheEntryDAO.getCacheEntryNotNull(SPY_CACHE_CODE);
        warCacheEntry = cacheEntryDAO.getCacheEntryNotNull(WAR_CACHE_CODE);
        //skip all spy fleet that is incomplete from last try
        Long spyRequestCode = spyCacheEntry.getLong();
        if(spyRequestCode != null)
            fleetDAO.findWithCode(spyRequestCode).forEach(fleet ->{
                fleet.closeFlyPlan();
                fleetDAO.saveOrUpdate(fleet);
            });
        cacheEntryDAO.setValue(SPY_CACHE_CODE, null);
        cacheEntryDAO.setValue(WAR_CACHE_CODE, null);
    }

    @Override
    public void onStep() {
        pause = SHORT_PAUSE;
        if(!isSlotsToUseValid()) return;

        spyCacheEntry = cacheEntryDAO.getCacheEntryNotNull(SPY_CACHE_CODE);
        warCacheEntry = cacheEntryDAO.getCacheEntryNotNull(WAR_CACHE_CODE);

        if(isTimeToSpyFarmWave()) {
            slotToUse = propertiesSlotToUse;
            explorationArea = typeExplorationArea();
            planer = new ColonyPlaner(typeReadyColony());
            if(!createSpyWave()) {
                System.out.println("Farm thread has empty spy wave than waiting 5 min and try again");
                pause = LONG_PAUSE;
                return;
            }
        }

        if (isTimeToAttackFarmWave()) {
            createAttackWave();
            cleanResourceOnAttackedTargets();
        }
    }

    private void cleanResourceOnAttackedTargets() {
        for(TargetEntity target : attackWave) {
            target.metal = 0L;
            target.crystal = 0L;
            target.deuterium = 0L;
            target.resources = 0L;
            targetDAO.saveOrUpdate(target);
        }
    }

    private void createAttackWave() {
        slotToUse = slotToUse == null ? propertiesSlotToUse : slotToUse;
        reloadSpyTargets();
        attackWave = selectAvailableTargets(selectTargetsWithoutDefense(), slotToUse);
        if(attackWave.isEmpty()) {
            System.err.println("Error: attackWave is empty");
            return;
        }
        attackWave.stream().filter(target -> target.updated == null ||
                target.updated.isBefore(spyCacheEntry.updated))
                .forEach(target -> System.err.println("Error: "+target+" has no loaded report "+
                        target.updated+" < "+spyCacheEntry.updated));

        spyCacheEntry.setLong(null);
        warCacheEntry.setLong(fleetDAO.generateNewCode());
        fleetDAO.createNewWave(Mission.ATTACK, attackWave, warCacheEntry.getLong());
        cacheEntryDAO.saveOrUpdate(warCacheEntry);
        cacheEntryDAO.saveOrUpdate(spyCacheEntry);
    }

    private List<TargetEntity> selectTargetsWithoutDefense() {
        List<TargetEntity> result = spyWave.stream()
                .filter(target -> target.fleetSum != null && target.defenseSum != null)
                .filter(target -> target.fleetSum == 0 && target.defenseSum == 0)
                .sorted(Comparator.comparingLong(o -> o.resources))
                .collect(Collectors.toList());
        Collections.reverse(result);
        return result;
    }

    private void reloadSpyTargets() {
        spyWave = spyWave.stream().map(targetDAO::fetch).collect(Collectors.toList());
    }

    private List<TargetEntity> selectAvailableTargets(List<TargetEntity> collect, int count) {
        Map<Integer, Long> flyPointsAvailability = new HashMap<>();
        instance.flyPoints.forEach(colony -> flyPointsAvailability.put(colony.cp, 0L));
        List<TargetEntity> result = new ArrayList<>();

        for (TargetEntity target: collect) {
            Long requiredTransporterSmall = target.calculateTransportByTransporterSmall();
            ColonyEntity colony = ColonyDAO.getInstance().fetch(new ColonyPlaner().getNearestColony(target));
            Long booked = flyPointsAvailability.get(colony.cp);
            if(colony.transporterSmall >= requiredTransporterSmall + booked) {
                flyPointsAvailability.put(colony.cp, booked + requiredTransporterSmall);
                result.add(target);
            } else {
                System.err.println("Error: "+colony+" has " + colony.transporterSmall + " transporterSmall and can not push " +
                        requiredTransporterSmall + " already booked "+ booked);
            }
            if(result.size() >= count) break;
        }

        System.out.print("farm wave "); flyPointsAvailability.forEach((key, value) -> System.out.print(ColonyDAO.getInstance().find(key) + " " + value + "ts, ")); System.out.println();

        return result.stream()
                .sorted(Comparator.comparingLong(o -> -o.toPlanet().calculateDistance(new ColonyPlaner().getNearestColony(o).toPlanet())))
                .collect(Collectors.toList());
    }

    public boolean createSpyWave() {
        spyWave = findNextFarms(slotToUse * FARM_SPY_SCALE); System.out.print("farm index="+cacheEntryDAO.getCacheEntryNotNull(FARM_INDEX_CACHE).getInt()+": "+spyWave.size());
        List<TargetEntity> richFarm = findRichFarm(slotToUse); System.out.print("+"+richFarm.size());
        spyWave.addAll(richFarm);
        spyWave = removeFarmsForWhichMissingShips();System.out.print("-removeFarmsForWhichMissingShips="+spyWave.size()+"\n");
        if (spyWave.isEmpty()) return false;

        warCacheEntry.setLong(null);
        spyCacheEntry.setLong(fleetDAO.generateNewCode());
        fleetDAO.createNewWave(Mission.SPY, spyWave, spyCacheEntry.getLong());
        cacheEntryDAO.saveOrUpdate(warCacheEntry);
        cacheEntryDAO.saveOrUpdate(spyCacheEntry);
        return true;
    }

    private List<TargetEntity> removeFarmsForWhichMissingShips() {
        return spyWave.stream()
                .filter(target -> {
                    ColonyEntity colony = ColonyDAO.getInstance().fetch(new ColonyPlaner().getNearestColony(target));
                    return colony.espionageProbe > 0 && colony.transporterSmall > 0;
                })
                .collect(Collectors.toList());
    }

    private List<TargetEntity> findNextFarms(Integer count) {
        Integer farmIndex = loadFarmIndex();
        int startIndex = count * farmIndex;
        int endIndex = startIndex + count -1;
        Integer indexCount = Instance.config.getConfigInteger(threadName, INDEX_COUNT_CONFIG, 8);
        List<TargetEntity> farms = findNearedFarms();
        if(farms.size()-1 < endIndex) {
            endIndex = farms.size() - 1;
            farmIndex = indexCount - 1;
            System.err.println("Error: "+threadName+"."+INDEX_COUNT_CONFIG+" is too high i result "+FARM_INDEX_CACHE+" is set to 0");
        }
        cacheEntryDAO.setValue(FARM_INDEX_CACHE, Integer.toString((farmIndex + 1) % indexCount));
        return farms.subList(startIndex, Math.min(farms.size()-1, endIndex));
    }

    private List<TargetEntity> findNearedFarms() {
        return targetDAO.findFarms()
                .stream()
                .filter(target -> planer.isNear(target, ColonyPlaner.mapSystemToDistance(explorationArea)))
                .collect(Collectors.toList());
    }

    private Integer typeExplorationArea() {
        Integer explorationArea = Instance.config.getConfigInteger(threadName, EXPLORATION_AREA_CONFIG, -1);
        if(explorationArea>=0) return explorationArea;
        return Instance.config.getConfigInteger(MAIN, EXPLORATION_AREA_CONFIG, -1);
    }

    private List<ColonyEntity> typeReadyColony() {
        List<ColonyEntity> readyColony = new ArrayList<>();
        Instance.getInstance().getFlyPoints().forEach(colonyEntity -> {
            ColonyEntity fetch = ColonyDAO.getInstance().fetch(colonyEntity);
            if(fetch.transporterSmall > 0 && fetch.espionageProbe > 0) {
                readyColony.add(fetch);
            }
        });
        return readyColony;
    }

    private Integer loadFarmIndex() {
        CacheEntryEntity cacheEntryNotNull = cacheEntryDAO.getCacheEntryNotNull(FARM_INDEX_CACHE);
        Integer index = cacheEntryNotNull.getInt();
        if(index == null || DateUtil.isExpired(cacheEntryNotNull.updated, 4, ChronoUnit.HOURS)) index = 0;
        return index ;
    }

    private List<TargetEntity> findRichFarm(Integer count) {
        return findNearedRichFarms()
                .stream()
                .filter(target -> new ColonyPlaner().isNear(target, ColonyPlaner.mapSystemToDistance(explorationArea)))
                .filter(target -> spyWave.stream()
                        .map(baseTarget -> baseTarget.id)
                        .noneMatch(baseTarget -> baseTarget.equals(target.id)))
                .limit(count)
                .collect(Collectors.toList());
    }

    private List<TargetEntity> findNearedRichFarms() {
        return targetDAO.findRichFarms()
                .stream()
                .filter(target -> planer.isNear(target, ColonyPlaner.mapSystemToDistance(explorationArea)))
                .collect(Collectors.toList());
    }

    private boolean isTimeToSpyFarmWave() {
        return spyCacheEntry.getLong() == null && isFleetAlmostBack(warCacheEntry.getLong());
    }

    public boolean isTimeToAttackFarmWave() {
        return warCacheEntry.getLong() == null && isFleetBack(spyCacheEntry.getLong());
    }

    public boolean isFleetBack(Long code) {
        if (code == null) return true;
        List<FleetEntity> spyFleets = new ArrayList<>(fleetDAO.findWithCode(code));
        if (spyFleets.isEmpty()) return true;
        if (spyFleets.stream().anyMatch(fleetEntity -> fleetEntity.start == null)) return false;

        LocalDateTime lastSpy = spyFleets.stream()
                .map(fleetEntity -> fleetEntity.visited)
                .max(LocalDateTime::compareTo)
                .get();

        return MessageService.getInstance().getLastChecked().isAfter(lastSpy);
    }

    public boolean isFleetAlmostBack(Long code) {
        if(code == null) return true;
        List<FleetEntity> withCode = fleetDAO.findWithCode(code);
        List<LocalDateTime> backTimes = withCode.stream()
                .map(fleetEntity -> fleetEntity.back)
                .sorted()
                .collect(Collectors.toList());
        LocalDateTime whenHalfIsBack = backTimes.get(backTimes.size() / 2);
        return withCode.isEmpty() ||  LocalDateTime.now().isAfter(whenHalfIsBack);
    }

    private boolean isSlotsToUseValid() {
        propertiesSlotToUse = Instance.config.getConfigInteger(threadName, SLOT_CONFIG, -1);
        if(propertiesSlotToUse == -1) propertiesSlotToUse = BaseSI.getInstance().getAvailableFleetCount(threadName);
        return propertiesSlotToUse > 0;
    }
}
