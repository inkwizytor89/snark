package org.enoch.snark.module.expedition;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.SI;
import org.enoch.snark.module.AbstractThread;

import java.util.*;

import static org.enoch.snark.db.entity.FleetEntity.EXPEDITION;

public class ExpeditionThread extends AbstractThread {

    public static final String threadName = "expedition";

    private final Instance instance;
    private Queue<ColonyEntity> expedyctionQueue = new LinkedList<>();
    private int pause = 120;

    public ExpeditionThread(SI si) {
        super(si);
        instance = si.getInstance();
    }

    @Override
    protected int getPauseInSeconds() {
        return pause;
    }

    @Override
    protected void onStart() {
        cleanExpeditions();
        expedyctionQueue.addAll(instance.universeEntity.colonyEntities);
    }

    @Override
    protected void onStep() {
        if (areFreeSlotsForExpedition() && noWaitingExpedition()) {
            ColonyEntity colony = expedyctionQueue.poll();
            FleetEntity expedition = FleetEntity.createExpeditionFleet(instance, colony.toPlanet());
            if(colony.canSent(expedition)) {
                setExpeditionReadyToStart(expedition);
            }
            expedyctionQueue.add(colony);
            pause = 20;
        } else {
            pause = 120;
        }
    }

    private boolean noWaitingExpedition() {
        return instance.commander.peekFleetQueue().stream()
                .filter(abstractCommand -> abstractCommand instanceof SendFleetCommand)
                .noneMatch(command -> FleetEntity.EXPEDITION.equals(((SendFleetCommand) command).fleet.type));
    }

    private boolean areFreeSlotsForExpedition() {
        return instance.commander.getExpeditionFreeSlots() > 0;
    }

    private void setExpeditionReadyToStart(FleetEntity expedition) {
        instance.commander.push(new SendFleetCommand(instance, expedition));
    }

    private void cleanExpeditions() {
        for(FleetEntity fleet : this.instance.daoFactory.fleetDAO.fetchAll()) {
            if(EXPEDITION.equals(fleet.type)) {
                this.instance.daoFactory.fleetDAO.remove(fleet);
            }
        }
    }
}
