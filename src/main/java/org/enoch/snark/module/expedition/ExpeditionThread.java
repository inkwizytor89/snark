package org.enoch.snark.module.expedition;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.BetaExpeditionFleetCommand;
import org.enoch.snark.gi.command.impl.ExpeditionFleetCommand;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.instance.SI;
import org.enoch.snark.module.AbstractThread;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static org.enoch.snark.db.entity.FleetEntity.EXPEDITION;
import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_BASE_FLEET;

public class ExpeditionThread extends AbstractThread {

    public static final String threadName = "expedition";

    private final Queue<ColonyEntity> expeditionQueue = new LinkedList<>();

    public ExpeditionThread(SI si) {
        super(si);
        pause = 1;
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
        cleanExpeditions();
        chooseColoniesForExpeditionsStart();
    }

    private void chooseColoniesForExpeditionsStart() {
        System.err.println("\nlist for expeditions:");
        List<ColonyEntity> planetList = ColonyDAO.getInstance().fetchAll()
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
            FleetEntity expedition = FleetEntity.createExpeditionFleet(colony);
            if(colony.canSent(expedition)) {
                setExpeditionReadyToStart(colony);
            } else System.err.println("Colony "+colony+" can not sent expedition");
            expeditionQueue.add(colony);
        }
    }

    private void checkColonyStatus(ColonyEntity colony) {
        instance.push(new OpenPageCommand(PAGE_BASE_FLEET, colony));
    }

    private boolean noWaitingExpedition() {
        return commander.peekQueues().stream().noneMatch(command -> command.getTags().contains(threadName));
    }

    private boolean areFreeSlotsForExpedition() {
        return instance.commander.getExpeditionFreeSlots() > 0;
    }

    private void setExpeditionReadyToStart(FleetEntity expedition) {
        instance.push(new BetaExpeditionFleetCommand(expedition));
    }

    private void setExpeditionReadyToStart(ColonyEntity colony) {
        ExpeditionFleetCommand expeditionFleetCommand = new ExpeditionFleetCommand(colony);
        expeditionFleetCommand.addTag(threadName);
        instance.push(expeditionFleetCommand);
    }

    private void cleanExpeditions() {
        for(FleetEntity fleet : FleetDAO.getInstance().fetchAll()) {
            if(EXPEDITION.equals(fleet.type)) {
                FleetDAO.getInstance().remove(fleet);
            }
        }
    }
}
