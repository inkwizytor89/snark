package org.enoch.snark.module.expedition;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.FleetDAO;
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
    public static final int SHORT_PAUSE = 20;
    public static final int LONG_PAUSE = 40;

    private final Instance instance;
    private final Queue<ColonyEntity> expeditionQueue = new LinkedList<>();
    private int pause = SHORT_PAUSE;

    public ExpeditionThread(SI si) {
        super(si);
        instance = si.getInstance();
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
        expeditionQueue.addAll(ColonyDAO.getInstance().fetchAll());
    }

    @Override
    protected void onStep() {
        if (areFreeSlotsForExpedition() && noWaitingExpedition()) {
            ColonyEntity colony = expeditionQueue.poll();
            FleetEntity expedition = FleetEntity.createExpeditionFleet(instance, colony.toPlanet());
            if(colony.canSent(expedition)) {
                setExpeditionReadyToStart(expedition);
            }
            expeditionQueue.add(colony);
            pause = SHORT_PAUSE;
        } else {
            pause = LONG_PAUSE;
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
        for(FleetEntity fleet : FleetDAO.getInstance().fetchAll()) {
            if(EXPEDITION.equals(fleet.type)) {
                FleetDAO.getInstance().remove(fleet);
            }
        }
    }
}
