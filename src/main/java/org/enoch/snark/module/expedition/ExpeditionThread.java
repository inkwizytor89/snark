package org.enoch.snark.module.expedition;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.ExpeditionFleetCommand;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.SI;
import org.enoch.snark.module.AbstractThread;

import java.util.*;

import static org.enoch.snark.db.entity.FleetEntity.EXPEDITION;
import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_BASE_FLEET;

public class ExpeditionThread extends AbstractThread {

    public static final String threadName = "expedition";
    public static final int SHORT_PAUSE = 20;
    public static final int LONG_PAUSE = 40;

    private final Instance instance;
    private final Queue<ColonyEntity> expeditionQueue = new LinkedList<>();

    public ExpeditionThread(SI si) {
        super(si);
        pause = SHORT_PAUSE;
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

            FleetEntity expedition = FleetEntity.createExpeditionFleet(colony);
            if(colony.canSent(expedition)) {
                setExpeditionReadyToStart(expedition);
            } else {
                checkColonyStatus(colony);
//                pause = 1;
            }
            pause = SHORT_PAUSE;
            expeditionQueue.add(colony);
        } else {
            pause = LONG_PAUSE;
        }
    }

    private void checkColonyStatus(ColonyEntity colony) {
        instance.push(new OpenPageCommand(PAGE_BASE_FLEET, colony));
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
        instance.push(new ExpeditionFleetCommand(expedition));
    }

    private void cleanExpeditions() {
        for(FleetEntity fleet : FleetDAO.getInstance().fetchAll()) {
            if(EXPEDITION.equals(fleet.type)) {
                FleetDAO.getInstance().remove(fleet);
            }
        }
    }
}
