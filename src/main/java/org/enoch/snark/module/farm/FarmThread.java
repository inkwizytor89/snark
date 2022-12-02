package org.enoch.snark.module.farm;

import org.enoch.snark.db.dao.FarmDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FarmEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.instance.SI;
import org.enoch.snark.model.Planet;
import org.enoch.snark.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FarmThread extends AbstractThread {

    private static final Logger log = Logger.getLogger(FarmThread.class.getName());
    public static final String threadName = "farm";

    private final FarmDAO farmDAO;
    private FarmEntity actualFarm;
    private LinkedList<TargetEntity> baseFarms = new LinkedList<>();
    private LinkedList<TargetEntity> randomFarms = new LinkedList<>();
    private List<TargetEntity> spyWave = new LinkedList<>();
    private List<TargetEntity> attackWave = new LinkedList<>();
    private int slotToUse = 4;

    public FarmThread(SI si) {
        super(si);
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
    protected void onStart() {
        super.onStart();
        FarmEntity lastFarm = farmDAO.getActualState();
        //if last farm have waiting spy fleet then should be closed
        if(lastFarm != null && lastFarm.spyRequestCode != null && lastFarm.warRequestCode == null) {
            for(FleetEntity fleet : fleetDAO.findWithCode(lastFarm.spyRequestCode)) {
                fleet.start = LocalDateTime.now();
                fleet.code = 0L;
                fleetDAO.saveOrUpdate(fleet);
            }
        }

        actualFarm = new FarmEntity();
        actualFarm.start = LocalDateTime.now();
        farmDAO.saveOrUpdate(actualFarm);
        findBestFarms();
    }

    @Override
    public void onStep() {
        calculateSlotsToUse();
        if (!fulfillsPreconditions()) return;

        if(isTimeToCreateWave()) {
            actualFarm = new FarmEntity();
            actualFarm.start = LocalDateTime.now();
            farmDAO.saveOrUpdate(actualFarm);
        }

        if(isTimeToSpyFarmWave()) {
            Long code = fleetDAO.genereteNewCode();
            createSpyWave();
//            spyWave = new LinkedList<>(targetDAO.findFarms(100));// todo remove when createSpyWave
            farmDAO.createNewWave(Mission.SPY, spyWave, code);
            actualFarm.spyRequestCode = code;
            farmDAO.saveOrUpdate(actualFarm);
        }
        if (isTimeToAttackFarmWave()) {
            Long code = fleetDAO.genereteNewCode();
            int fleetNum = si.getAvailableFleetCount(this);
            if(fleetNum < 1) {
                return;
            }
            createAttackWave(fleetNum);
//            attackWave = new LinkedList<>(targetDAO.findTopFarms(fleetNum));
            farmDAO.createNewWave(Mission.ATTACK, attackWave, code);
            actualFarm.warRequestCode = code;
            farmDAO.saveOrUpdate(actualFarm);
        }
    }

    private void createAttackWave(int count) {
        attackWave = spyWave.stream()
                .filter(target -> target.fleetSum == 0 && target.defenseSum == 0)
                .sorted(Comparator.comparingLong(o -> o.resources))
                .collect(Collectors.toList());
        Collections.reverse(attackWave);
        List<TargetEntity> collect = attackWave.stream()
                .limit(count)
                .sorted(Comparator.comparingLong(o -> -o.toPlanet().calculateDistance(instance.findNearestFlyPoint(o).toPlanet())))
                .collect(Collectors.toList());
        attackWave = collect;
    }

    public void createSpyWave() {
        int spyWaveSize = slotToUse * 2;
        if(baseFarms.size() > spyWaveSize) {
            // add tops farms
            spyWave = baseFarms.subList(0, spyWaveSize);

            // add random probe
            if(randomFarms.size() < slotToUse) {
                randomFarms = new LinkedList<>(baseFarms.subList(spyWaveSize, baseFarms.size()));
            }
            for(int i=0;i<slotToUse*2;i++) spyWave.add(randomFarms.poll());

            //remove last attacked farms
            spyWave = spyWave.stream()
                    .filter(target -> !attackWave.contains(target))
                    .collect(Collectors.toList());
        } else {
            spyWave = new LinkedList<>(baseFarms.subList(0,baseFarms.size() -1)) ;
        }
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

    public boolean fulfillsPreconditions() {
        pause = 1;
        if(baseFarms.size() < slotToUse * 4) {
            pause = 60;
            System.err.println("farm step skipping");
            return false;
        }
        return true;
    }

    public boolean isFleetBack(Long code) {
        return code != null && fleetDAO.fetchAll().stream()
            .filter(fleet -> code.equals(fleet.code))
            .allMatch(FleetEntity::isItBack);
    }

    public boolean isFleetAlmostBack(Long code) {
        return code != null && fleetDAO.fetchAll().stream()
                .filter(fleet -> code.equals(fleet.code))
                .anyMatch(FleetEntity::isItBack);
    }

    public void findBestFarms() {
        List<TargetEntity> farmsInRange = targetDAO.findFarms(Integer.MAX_VALUE).stream()
                .filter(target -> target.energy != null && target.energy > 0)
                .filter(this::isNear)
                .sorted(Comparator.comparingLong(o -> o.energy))
                .collect(Collectors.toList());
        Collections.reverse(farmsInRange); // starts witch most energy ed planets
        this.baseFarms = new LinkedList<>(farmsInRange);
    }

    private boolean isNear(TargetEntity targetEntity) {
        Planet targetPlanet = targetEntity.toPlanet();
        ColonyEntity nearestSource = instance.findNearestFlyPoint(targetPlanet);
        Integer distance = targetPlanet.calculateDistance(nearestSource.toPlanet());
        return distance < 10000;
    }

    private void calculateSlotsToUse() {
        slotToUse = si.getAvailableFleetCount(this);
    }
}
