package org.enoch.snark.module.expedition;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.ExpeditionFleetCommand;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.instance.Commander;
import org.enoch.snark.instance.SI;
import org.enoch.snark.module.AbstractThread;

import java.util.*;
import java.util.stream.Collectors;

public class ExpeditionThread extends AbstractThread {

    public static final String threadName = "expedition";

    private final Queue<ColonyEntity> expeditionQueue = new LinkedList<>();
    private final ColonyDAO colonyDAO;
    private Long maxTL;

    public ExpeditionThread(SI si) {
        super(si);
        pause = 1;
        colonyDAO = ColonyDAO.getInstance();
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
        chooseColoniesForExpeditionsStart();
    }

    private void chooseColoniesForExpeditionsStart() {
        System.err.println("\nlist for expeditions:");
        List<ColonyEntity> planetList = colonyDAO.fetchAll()
                .stream()
                .filter(colonyEntity -> colonyEntity.isPlanet)
                .sorted(Comparator.comparing(o -> -o.galaxy))
                .collect(Collectors.toList());
        for(ColonyEntity planet : planetList) {
            ColonyEntity colony = planet;
            if(planet.cpm != null) {
                colony = ColonyDAO.getInstance().find(planet.cpm);
            }
            expeditionQueue.add(colony);
            System.err.println(colony);
        }
        System.err.println();
    }

    @Override
    protected void onStep() {
        if (areFreeSlotsForExpedition() && noWaitingExpedition()) {
            ColonyEntity colony = expeditionQueue.poll();
            FleetEntity expedition = buildExpeditionFleet(colony);
            if(expedition != null) {
                System.err.println(colony+": "+expedition.source+" -> "+expedition.getDestination());
                setExpeditionReadyToStart(expedition);
            }
            expeditionQueue.add(colony);
        }
    }

    private boolean noWaitingExpedition() {
        return commander.peekQueues().stream().noneMatch(command -> command.getTags().contains(threadName));
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
        }
        return sendWhatYouCan();
    }

    private boolean anyExpeditionStartPointHasEnoughShips(Map<ShipEnum, Long> expeditionMap) {
        return expeditionQueue.stream().anyMatch(colony -> colony.hasEnoughShips(expeditionMap));
    }

    private FleetEntity sendWhatYouCan() {
        System.err.println("\nsendWhatYouCan expedition - why?!\n");
        ColonyEntity anotherExpeditionStartPoint = findBestExpeditionStartPoint();
        FleetEntity expedition = FleetEntity.createExpedition(anotherExpeditionStartPoint);
        if(anotherExpeditionStartPoint.explorer > 0) {
            expedition.explorer = 1L;
        }
        expedition.transporterLarge = anotherExpeditionStartPoint.transporterLarge;
        expedition.transporterSmall = (maxTL - expedition.transporterLarge) * 5;
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
