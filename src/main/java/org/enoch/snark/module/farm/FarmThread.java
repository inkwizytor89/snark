package org.enoch.snark.module.farm;

import org.enoch.snark.db.dao.FarmDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FarmEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.command.request.SendFleetRequest;
import org.enoch.snark.instance.SI;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.types.ColonyType;
import org.enoch.snark.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
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
    private FarmEntity previousFarm;
    private LinkedList<TargetEntity> farms = new LinkedList<>();

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
        if(farmDAO.getActualState() == null) {
//            farmDAO.saveOrUpdate(createFarmEntity());
            farmDAO.saveOrUpdate(createFarmEntity());
        }
        actualFarm = farmDAO.getActualState();
        previousFarm = farmDAO.getPreviousState();
    }

    private FarmEntity createFarmEntity() {
        FarmEntity farmEntity = new FarmEntity();
        farmEntity.start = LocalDateTime.now();
        return farmEntity;
    }

    @Override
    public void onStep() {
        pause = 1;
        System.err.println("farm start");

        if(farms.isEmpty()) {
            findBestFarms();
        }
        if(farms.size() < calculateSlotsToUse() * 4) {
            pause = 60;
            System.err.println("farm skipping");
            return;
        }
        // teraz na such request do skanowania jakiegos zbioru 4x slots

        if(isTimeToNewFarmWave()) {
            prepereNewFarmWave();
        } else if(isTimeToSpyCheckFarms()) {
            System.err.println("start "+ actualFarm.start);
            List<TargetEntity> farmTargets = targetDAO.findFarms(20);

            //new transaction
            actualFarm.spyRequestCode = fleetDAO.genereteNewCode();
            for(TargetEntity farm : farmTargets) {
                FleetEntity spyFleet = FleetEntity.createSpyFleet(farm);
                spyFleet.spaceTarget = ColonyType.PLANET.getName();
                spyFleet.code = actualFarm.spyRequestCode;
                FleetDAO.getInstance().saveOrUpdate(spyFleet);
            }
//            actualFarm.spyRequestCode = new SendFleetRequest(si.getInstance(), FleetEntity.SPY, farmTargets).sendAndWait();

            farmDAO.saveOrUpdate(actualFarm);
            //end transaction
            // wyciagnij poprzedni zbior celow

            // wysylac sondy 50 sond do target ktore maja najwieszy porencjal a nie bylu ostatnio oblatywane
            // spyrequest zrobic mu konsrtuktor ktory dziala na tych zbiorach lub lepiej
        } else if (actualFarm.spyRequestCode != null) {
            System.err.println("spyRequestCode "+ actualFarm.spyRequestCode);
            int fleetNum = si.getAvailableFleetCount(this);
            List<TargetEntity> farmTargets = targetDAO.findTopFarms(fleetNum);
            actualFarm.warRequestCode = new SendFleetRequest(si.getInstance(), FleetEntity.ATTACK, farmTargets)
                    .setLimit(fleetNum)
                    .sendAndWait();
            farmDAO.saveOrUpdate(actualFarm);
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

    public boolean isTimeToSpyCheckFarms() {
        return LocalDateTime.now().isAfter(actualFarm.start);
    }

    public void prepereNewFarmWave() {
        System.err.println("warRequestCode "+ actualFarm.warRequestCode);
        previousFarm = actualFarm;
        actualFarm = createFarmEntity();
        farmDAO.saveOrUpdate(actualFarm);
        // x = przylot ostatniego statku
        // stworz nowy farm entity
        // ustaw jego start na x-5min
        // aktualny actualFarm ustaw jako nowy
    }

    public boolean isTimeToNewFarmWave() {
        return actualFarm.warRequestCode != null && fleetDAO.fetchAll().stream()
    .filter(fleet -> actualFarm.warRequestCode.equals(fleet.code))
    .allMatch(FleetEntity::isItBack);
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
                .filter(target -> target.energy > 0)
                .filter(this::isNear)
                .sorted(Comparator.comparingLong(o -> o.energy))
                .collect(Collectors.toList());
        Collections.reverse(farmsInRange); // starts witch most energy ed planets
        this.farms = new LinkedList<>(farmsInRange);
    }

    private boolean isNear(TargetEntity targetEntity) {
        Planet targetPlanet = targetEntity.toPlanet();
        ColonyEntity nearestSource = instance.findNearestMoon(targetPlanet);
        Integer distance = targetPlanet.calculateDistance(nearestSource.toPlanet());
        return distance < 10000;
    }

    private int calculateSlotsToUse() {
        int fleetMax = commander.getFleetMax();
        int expeditionMax = commander.getExpeditionMax();
        return fleetMax - expeditionMax - 2;
    }
}
