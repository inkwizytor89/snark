package org.enoch.snark.module.expedition;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.ExpeditionFleetCommand;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.instance.Commander;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.module.AbstractThread;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_BASE_FLEET;

public class ExpeditionThread extends AbstractThread {

    public static final String threadName = "expedition";

    private final Queue<ColonyEntity> expeditionQueue = new LinkedList<>();
    private Long maxTL;

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
        pause = 5;
        chooseColoniesForExpeditionsStart();
    }

    private void chooseColoniesForExpeditionsStart() {
        expeditionQueue.addAll(instance.flyPoints);
    }

    @Override
    protected void onStep() {
        if (areFreeSlotsForExpedition() && noWaitingExpedition()) {
            ColonyEntity colony = getNextFlyPoint();
            if (colony == null) return;
            FleetEntity expedition = buildExpeditionFleet(colony);
            if(expedition != null) {
                setExpeditionReadyToStart(expedition);
            }
        }
    }

    private ColonyEntity getNextFlyPoint() {
        ColonyEntity poll = expeditionQueue.poll();
        if(poll == null) {
            System.err.println("expeditionQueue in ExpeditionThread is empty");
            return null;
        } else {
            expeditionQueue.add(poll);
            return ColonyDAO.getInstance().fetch(poll);
        }
    }

    private boolean noWaitingExpedition() {
        return noWaitingElementsByTag(threadName);
    }

    private boolean areFreeSlotsForExpedition() {
        return Commander.getInstance().getExpeditionFreeSlots() > 0;
    }

    private FleetEntity buildExpeditionFleet(ColonyEntity colony) {
        maxTL = instance.calculateMaxExpeditionSize();
        FleetEntity expeditionToSend = FleetEntity.createExpedition(colony);

        Map<ShipEnum, Long> expeditionMap = ShipEnum.createExpeditionMap(maxTL, 0L, 1L);
        if(colony.hasEnoughShips(expeditionMap)) {

            expeditionToSend.setShips(expeditionMap);
            return expeditionToSend;
        }
        if(anyExpeditionStartPointHasEnoughShips(expeditionMap)) {
            return null;
        } else {
            loadExpeditionPoints();
        }
        return sendWhatYouCan();
    }

    private boolean anyExpeditionStartPointHasEnoughShips(Map<ShipEnum, Long> expeditionMap) {
        return expeditionQueue.stream().anyMatch(colony -> colony.hasEnoughShips(expeditionMap));
    }

    private void loadExpeditionPoints() {
        expeditionQueue.forEach(col -> commander.push(new OpenPageCommand(PAGE_BASE_FLEET, col)));
        System.err.println("reloading expedition points");
        SleepUtil.secondsToSleep(60);
    }

    private FleetEntity sendWhatYouCan() {
        System.err.println("\nsendWhatYouCan expedition - why?!\n");
        ColonyEntity anotherExpeditionStartPoint = findBestExpeditionStartPoint();
        FleetEntity expedition = FleetEntity.createExpedition(anotherExpeditionStartPoint);
        if(anotherExpeditionStartPoint.explorer > 0) {
            expedition.explorer = 1L;
        }
        if(anotherExpeditionStartPoint.transporterLarge > maxTL) {
            expedition.transporterLarge = maxTL;
        } else {
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
        for (ColonyEntity expPoint : expeditionQueue) {
            long amount = 5*expPoint.transporterLarge + expPoint.transporterSmall;
            if(amount > bestAmount) {
                bestAmount = amount;
                bestColony = expPoint;
            }
        }
        return bestColony;
    }

    private void setExpeditionReadyToStart(FleetEntity expedition) {
        ExpeditionFleetCommand expeditionFleetCommand = new ExpeditionFleetCommand(expedition);
        expeditionFleetCommand.addTag(threadName);
        instance.push(expeditionFleetCommand);
    }
}
