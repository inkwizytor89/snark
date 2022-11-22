package org.enoch.snark.module.farm;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.FarmDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FarmEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.instance.SI;
import org.enoch.snark.model.Planet;
import org.enoch.snark.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FarmThread extends AbstractThread {

    public static final String threadName = "farm";
    private static final Logger log = Logger.getLogger(FarmThread.class.getName());
    public static final int MIN_FARM_COUNT = 40;

    private final FarmDAO farmDAO;
    private final FleetDAO fleetDAO;
    private final TargetDAO targetDAO;
    private FarmEntity actualFarm;
    private LinkedList<TargetEntity> baseFarms = new LinkedList<>();
    private LinkedList<TargetEntity> randomFarms = new LinkedList<>();
    private List<TargetEntity> spyWave = new LinkedList<>();
    private List<TargetEntity> attackWave = new LinkedList<>();
    private int slotToUse = 4;

    public FarmThread(SI si) {
        super(si);
        farmDAO = FarmDAO.getInstance();
        targetDAO = TargetDAO.getInstance();
        fleetDAO = FleetDAO.getInstance();
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
        actualFarm = farmDAO.getActualState();
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


//            actualFarm.warRequestCode = new SendFleetRequest(si.getInstance(), FleetEntity.ATTACK, farmTargets)
//                    .setLimit(fleetNum)
//                    .sendAndWait();
//            farmDAO.saveOrUpdate(actualFarm);
            // zapytaj o 50 najnowszych skanow
            // posortuj je wg korzystnosci i bez obrony
            // wybierz fleetNum najbardziej korzystnych
            // posegreguj je wg najdluższego lotu
            // stworz war request - niech ma konstruktor ktoremu podajesz cele
            // i podajesz ile ma z tego ruszyc, bo jak by jakis byl nie wypalem z powodu
            // bledu albo pozniej ze z falangi sie nie oplaca to zeby wziol nastepny
        }
        // niech czeka ile trzeba a nie aktywuje sie co sekunde
    }

    private void createAttackWave(int count) {
        if(spyWave.isEmpty()) {
            actualFarm.spyRequestCode = null;
            farmDAO.saveOrUpdate(actualFarm);
            return;
        }
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
        return actualFarm.spyRequestCode == null;
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

//    @Override
//    public void onStep() {
//        pause = 1;
//        System.err.println("farm start");
//
//        if(farms.isEmpty()) {
//            findBestFarms();
//        }
//        if(farms.size() < calculateSlotsToUse() * 4) {
//            pause = 60;
//            System.err.println("farm skipping");
//            return;
//        }
//        // teraz na such request do skanowania jakiegos zbioru 4x slots
//
//        if(actualFarm.warRequestCode != null) {
//            System.err.println("warRequestCode "+ actualFarm.warRequestCode);
//            previousFarm = actualFarm;
//            actualFarm = createFarmEntity();
//            farmDAO.saveOrUpdate(actualFarm);
//            // x = przylot ostatniego statku
//            // stworz nowy farm entity
//            // ustaw jego start na x-5min
//            // aktualny actualFarm ustaw jako nowy
//        } else if (actualFarm.spyRequestCode != null) {
//            System.err.println("spyRequestCode "+ actualFarm.spyRequestCode);
//            int fleetNum = si.getAvailableFleetCount(this);
//            List<TargetEntity> farmTargets = targetDAO.findTopFarms(fleetNum);
//            actualFarm.warRequestCode = new SendFleetRequest(si.getInstance(), FleetEntity.ATTACK, farmTargets)
//                    .setLimit(fleetNum)
//                    .sendAndWait();
//            farmDAO.saveOrUpdate(actualFarm);
//            // zapytaj o 50 najnowszych skanow
//            // posortuj je wg korzystnosci i bez obrony
//            // wybierz fleetNum najbardziej korzystnych
//            // posegreguj je wg najdluższego lotu
//            // stworz war request - niech ma konstruktor ktoremu podajesz cele
//            // i podajesz ile ma z tego ruszyc, bo jak by jakis byl nie wypalem z powodu
//            // bledu albo pozniej ze z falangi sie nie oplaca to zeby wziol nastepny
//        } else if(LocalDateTime.now().isAfter(actualFarm.start)) {
//            System.err.println("start "+ actualFarm.start);
//            List<TargetEntity> farmTargets = targetDAO.findFarms(20);
//            actualFarm.spyRequestCode = new SendFleetRequest(si.getInstance(), FleetEntity.SPY, farmTargets)
//                    .sendAndWait();
//            farmDAO.saveOrUpdate(actualFarm);
//            // wyciagnij poprzedni zbior celow
//
//            // wysylac sondy 50 sond do target ktore maja najwieszy porencjal a nie bylu ostatnio oblatywane
//            // spyrequest zrobic mu konsrtuktor ktory dziala na tych zbiorach lub lepiej
//        }
//            // niech czeka ile trzeba a nie aktywuje sie co sekunde
//    }

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
