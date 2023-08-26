package org.enoch.snark.module.farm;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.FarmDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FarmEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.instance.BaseSI;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.ColonyPlaner;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.service.MessageService;
import org.enoch.snark.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FarmThread extends AbstractThread {

    private static final Logger log = Logger.getLogger(FarmThread.class.getName());
    public static final String threadName = "farm";
    public static final int FARM_INDEX_COUNT = 7;
    public static final int FARM_SPY_SCAEL = 2;

    private final FarmDAO farmDAO;
    private FarmEntity actualFarm;
    private LinkedList<TargetEntity> randomFarms = new LinkedList<>();
    private List<TargetEntity> spyWave = new LinkedList<>();
    private List<TargetEntity> attackWave = new LinkedList<>();
    private int slotToUse = 4;
    private int farmIndex = 0;

    public FarmThread() {
        farmDAO = FarmDAO.getInstance();
    }

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
        pause = 4;
        FarmEntity lastFarm = farmDAO.getActualState();
        //if last farm have waiting spy fleet then should be closed
        if(lastFarm != null && lastFarm.spyRequestCode != null && lastFarm.warRequestCode == null) {
            for(FleetEntity fleet : fleetDAO.findWithCode(lastFarm.spyRequestCode)) {
                fleet.start = LocalDateTime.now();
                fleet.back = LocalDateTime.now();
                fleet.code = 0L;
                fleetDAO.saveOrUpdate(fleet);
            }
        }
    }

    @Override
    public void onStep() {
        calculateSlotsToUse();
        if(isTimeToCreateWave()) {
            actualFarm = new FarmEntity();
            actualFarm.start = LocalDateTime.now();
            farmDAO.saveOrUpdate(actualFarm);
        }

        if(isTimeToSpyFarmWave()) {
            actualFarm.spyRequestCode = fleetDAO.generateNewCode();
            createSpyWave();
            farmDAO.saveOrUpdate(actualFarm);
        }
        if (isTimeToAttackFarmWave()) {
            System.err.println("Spy wawe count "+spyWave.size());
            actualFarm.warRequestCode = fleetDAO.generateNewCode();
            createAttackWave(slotToUse);
            farmDAO.saveOrUpdate(actualFarm);
            cleanResourceOnAttackedTargets(attackWave);
        }
    }

    private void printResource(TargetEntity target) {
        System.err.println(target+" "+target.metal+" "+target.crystal+" "+target.deuterium);
    }

    private void cleanResourceOnAttackedTargets(List<TargetEntity> attackWave) {
        for(TargetEntity target : attackWave) {
            target.metal = 0L;
            target.crystal = 0L;
            target.deuterium = 0L;
            target.resources = 0L;
            targetDAO.saveOrUpdate(target);
        }
    }

    private void createAttackWave(int count) {
        spyWave = spyWave.stream().map(targetDAO::fetch).collect(Collectors.toList());
        List<TargetEntity> collect = spyWave.stream()
                .filter(target -> target.fleetSum != null && target.defenseSum != null)
                .filter(target -> target.fleetSum == 0 && target.defenseSum == 0)
                .sorted(Comparator.comparingLong(o -> o.resources))
                .collect(Collectors.toList());
        Collections.reverse(collect);

        attackWave = selectAvailableTargets(collect, count);
        if(!attackWave.isEmpty())
        attackWave.stream().filter(target -> target.updated ==null ||
                target.updated.isBefore(actualFarm.start))
                .forEach(target -> System.err.println("Error: "+target+" has no loaded report "+
                        target.updated+" < "+actualFarm.start));
        else  {
            System.err.println("Error: attackWave is empty");
            return;
        }

//        System.err.println("attack Wave count "+attackWave.size());
//        attackWave.forEach(this::printResource);

        farmDAO.createNewWave(Mission.ATTACK, attackWave, actualFarm.warRequestCode);
    }

    private List<TargetEntity> selectAvailableTargets(List<TargetEntity> collect, int count) {
        Map<Integer, Long> flyPointsAvailability = new HashMap<>();
        instance.flyPoints.forEach(colony -> flyPointsAvailability.put(colony.cp, 0L));
        List<TargetEntity> result = new ArrayList<>();

        for (TargetEntity target: collect) {
            Long requiredTransporterSmall = target.calculateTransportByTransporterSmall();
            ColonyEntity colony = ColonyDAO.getInstance().fetch(new ColonyPlaner(target).getNearestColony());
            Long booked = flyPointsAvailability.get(colony.cp);
            if(colony.transporterSmall >= requiredTransporterSmall + booked) {
                flyPointsAvailability.put(colony.cp, booked + requiredTransporterSmall);
                result.add(target);
            } else {
                System.err.println(colony+" has " + colony.transporterSmall + " transporterSmall and can not push " +
                        requiredTransporterSmall + " already booked "+ booked);
            }
            if(result.size() >= count) break;
        }

        flyPointsAvailability.forEach((key, value) ->
                System.err.println(ColonyDAO.getInstance().find(key) + " plan to send " + value + " TransporterSmall"));

        return result.stream()
                .sorted(Comparator.comparingLong(o -> -o.toPlanet().calculateDistance(new ColonyPlaner(o).getNearestColony().toPlanet())))
                .collect(Collectors.toList());
    }

    public void createSpyWave() {
        int spyWaveSize = slotToUse * FARM_SPY_SCAEL;
        farmIndex = farmIndex % FARM_INDEX_COUNT;
        int startIndex = spyWaveSize * farmIndex;
        int endIndex = startIndex + spyWaveSize -1;
        System.err.println("farmIndex="+farmIndex+" startIndex="+startIndex+" endIndex="+endIndex);

        List<TargetEntity> baseFarms = targetDAO.findFarms(spyWaveSize * (FARM_INDEX_COUNT+1))
                .stream()
                .filter(this::isNear)
                .collect(Collectors.toList());
        System.err.println("total="+baseFarms.size()+" before filter "+baseFarms.size());

        int realEnd = 0;
        if(baseFarms.size()<endIndex) {
            realEnd = baseFarms.size()-1;
            farmIndex = 0;
        }
        else realEnd = endIndex;
        spyWave = baseFarms.subList(startIndex, realEnd);

        List<TargetEntity> fatFarmsFromDatabase = targetDAO.findFatFarms(slotToUse);
        System.err.println("fat farm count "+ fatFarmsFromDatabase.size() + "/"+slotToUse);
        List<TargetEntity> fatFarms = fatFarmsFromDatabase
                .stream()
                .filter(this::isNear)
                .filter(target -> baseFarms.stream()
                        .map(baseTarget -> baseTarget.id)
                        .noneMatch(baseTarget -> baseTarget.equals(target.id)))
                .collect(Collectors.toList());
        fatFarms.forEach(targetEntity -> System.err.println("fat farm "+ targetEntity));
        spyWave.addAll(fatFarms);

        farmDAO.createNewWave(Mission.SPY, spyWave, actualFarm.spyRequestCode);
        farmIndex++;
    }

    private boolean isTimeToCreateWave() {
        return actualFarm == null || (actualFarm.warRequestCode != null && isFleetAlmostBack(actualFarm.warRequestCode));
    }

    public boolean isTimeToSpyFarmWave() {
        return actualFarm.spyRequestCode == null && slotToUse > 0;
    }

    public boolean isTimeToAttackFarmWave() {
        return actualFarm.warRequestCode == null && isFleetBack(actualFarm.spyRequestCode);
    }

    public boolean isFleetBack(Long code) {
        if (code == null) return false;
        List<FleetEntity> spyFleets = new ArrayList<>(fleetDAO.findWithCode(code));

        if(spyFleets.stream().anyMatch(fleetEntity -> fleetEntity.start == null)) return false;

        LocalDateTime lastSpy = spyFleets.stream()
                .map(fleetEntity -> fleetEntity.visited)
                .max(LocalDateTime::compareTo)
                .get();

        return MessageService.getInstance().getLastChecked().isAfter(lastSpy);
    }

    public boolean isFleetAlmostBack(Long code) {
        if(code == null){
            return false;
        }
        List<FleetEntity> withCode = fleetDAO.findWithCode(code);
        return withCode.isEmpty() || withCode.stream()
                .anyMatch(FleetEntity::isItBack);
    }

    private boolean isNear(TargetEntity targetEntity) {
        Planet targetPlanet = targetEntity.toPlanet();
        ColonyEntity nearestSource = new ColonyPlaner(targetPlanet).getNearestColony();
        Long distance = targetPlanet.calculateDistance(nearestSource.toPlanet());
        return distance < 13000;
    }

    private void calculateSlotsToUse() {
        slotToUse = BaseSI.getInstance().getAvailableFleetCount();
    }
}
