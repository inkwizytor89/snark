package org.enoch.snark.instance.si.module.expedition;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.ExpeditionFleetCommand;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.gi.types.ShipEnum;
import org.enoch.snark.instance.commander.Commander;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ConfigMap;

import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static org.enoch.snark.gi.types.UrlComponent.FLEETDISPATCH;
// na start mogl by przeleceic swoje flypointy
// jak sa starsze niz 4h to powinien go sobie zaktualizowac flypoint
// z tych co zostały znajdz najlepszego ?

public class ExpeditionThread extends AbstractThread {

    public static final String threadName = "expedition";
    public static final String BATTLE_EXTENSION = "battle_extension";
    public static final String MAX_DT = "max_dt";

    private Queue<ColonyEntity> expeditionSource = new LinkedList<>();
    private Long maxTL;

    public ExpeditionThread(ConfigMap map) {
        super(map);
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
        return Instance.commander.getExpeditionMax();
    }

    @Override
    protected void onStart() {
        super.onStart();
        pause = 8;
    }

    private void chooseColoniesForExpeditionsStart() {
        expeditionSource = new LinkedList<>();
        expeditionSource.addAll(map.getSources());
        loadExpeditionPoints();
    }

    @Override
    protected void onStep() {

//        Boolean waiting = map.getConfigBoolean("waiting", true);
//        if(waiting && stillWaitingForFleet()) return;
//        if(stillWaitingForFleet()) return;
        if (areFreeSlotsForExpedition() && noWaitingExpedition()) {
            ColonyEntity colony = getSource();
            if (colony == null) return;
            FleetEntity expedition = buildExpeditionFleet(colony);
            if(expedition != null) {
                setExpeditionReadyToStart(expedition);
            }
        }
    }

    private ColonyEntity getSource() {
        if(expeditionSource.isEmpty()) chooseColoniesForExpeditionsStart();
        ColonyEntity poll = expeditionSource.poll();
        if(poll == null) {
            System.err.println("expeditionQueue in ExpeditionThread is empty");
            chooseColoniesForExpeditionsStart();
            return null;
        } else {
            expeditionSource.add(poll);
            return ColonyDAO.getInstance().fetch(poll);
        }
    }

    private boolean noWaitingExpedition() {
        return commander.noBlockingHashInQueue(threadName);
    }

    private boolean areFreeSlotsForExpedition() {
        return Commander.getInstance().getExpeditionFreeSlots() > 0;
    }

    private FleetEntity buildExpeditionFleet(ColonyEntity colony) {
        maxTL = calculateMaxExpeditionSize();
        FleetEntity expeditionToSend = FleetEntity.createExpeditionDirection(colony);

        Map<ShipEnum, Long> requestedExpeditionShipMap = ShipEnum.createExpeditionShipMap(maxTL, 0L, 1L);
        if(colony.hasEnoughShips(requestedExpeditionShipMap)) {

            expeditionToSend.setShips(requestedExpeditionShipMap);
            return expeditionToSend;
        }

        if(anyExpeditionStartPointHasEnoughShips(requestedExpeditionShipMap)) {
            return null;
        } else if(allExpeditionStartPointHasNoneTransporters()) {
            return null;
        }
        return sendWhatYouCan();
    }

    private boolean allExpeditionStartPointHasNoneTransporters() {
        refreshColoniesFromDb();
        return expeditionSource.stream().allMatch(colony -> colony.transporterLarge<1 && colony.transporterSmall<1);
    }

    private void refreshColoniesFromDb() {
        expeditionSource.forEach(colonyEntity -> ColonyDAO.getInstance().refresh(colonyEntity));
    }

    private boolean anyExpeditionStartPointHasEnoughShips(Map<ShipEnum, Long> expeditionMap) {
        refreshColoniesFromDb();
        return expeditionSource.stream().anyMatch(colony -> colony.hasEnoughShips(expeditionMap));
    }

    private void loadExpeditionPoints() {
        refreshColoniesFromDb();
        expeditionSource.stream()
                .filter(colonyEntity -> DateUtil.isExpired(colonyEntity.updated, 2L, ChronoUnit.HOURS))
                .forEach(col -> {
                    new OpenPageCommand(FLEETDISPATCH, col).hash(threadName+"_"+col).push();
                });
        System.err.println("reloading expedition points");
        SleepUtil.secondsToSleep(expeditionSource.size() * 5L);
    }

    private FleetEntity sendWhatYouCan() {
        System.err.println("\nsendWhatYouCan expedition - why?!\n");
        ColonyEntity anotherExpeditionStartPoint = findBestExpeditionStartPoint();
        FleetEntity expedition = FleetEntity.createExpeditionDirection(anotherExpeditionStartPoint);
        if(anotherExpeditionStartPoint.explorer > 0) {
            expedition.explorer = 1L;
        }
        if(anotherExpeditionStartPoint.transporterLarge > maxTL) {
            expedition.transporterLarge = maxTL;
        } else {// send what you have
            expedition.transporterLarge = anotherExpeditionStartPoint.transporterLarge;
            long missingLT = (maxTL - expedition.transporterLarge) * 5;
            Long actualTransporterSmall = anotherExpeditionStartPoint.transporterSmall;
            expedition.transporterSmall = missingLT>actualTransporterSmall?actualTransporterSmall:missingLT;
        }
        return expedition;
    }

    private ColonyEntity findBestExpeditionStartPoint() {
        ColonyEntity bestColony = null;
        long bestAmount = 0L;
        for (ColonyEntity expPoint : expeditionSource) {
            long amount = 5*expPoint.transporterLarge + expPoint.transporterSmall;
            if(amount > bestAmount) {
                bestAmount = amount;
                bestColony = expPoint;
            }
        }
        return bestColony;
    }

    public Long calculateMaxExpeditionSize() {
        return map.getConfigLong(MAX_DT, 2500L);
    }

    private void setExpeditionReadyToStart(FleetEntity expedition) {
        boolean battleExtension = map.getConfigBoolean(BATTLE_EXTENSION, true);
        ExpeditionFleetCommand expeditionFleetCommand = new ExpeditionFleetCommand(expedition, battleExtension);
        expeditionFleetCommand.hash(threadName);
        expeditionFleetCommand.push();
    }
}
