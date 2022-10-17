package org.enoch.snark.module.expedition;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.BetaExpeditionFleetCommand;
import org.enoch.snark.gi.command.impl.ExpeditionFleetCommand;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.SI;
import org.enoch.snark.module.AbstractThread;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static org.enoch.snark.db.entity.FleetEntity.EXPEDITION;
import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_BASE_FLEET;

public class ExpeditionThread extends AbstractThread {

    public static final String threadName = "expedition";
    public static final int SHORT_PAUSE = 1;
    public static final int LONG_PAUSE = 1;
    private int failCount = 0;

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
        List<ColonyEntity> moons = ColonyDAO.getInstance().fetchAll()
                .stream().filter(colonyEntity -> !colonyEntity.isPlanet).collect(Collectors.toList());
        System.err.println("list for expeditions");
        moons.forEach(System.err::println);
        expeditionQueue.addAll(moons);
    }

    @Override
    protected void onStep() {
        if (areFreeSlotsForExpedition() && noWaitingExpedition()) {
            ColonyEntity colony = expeditionQueue.poll();
            failCount++;
            setExpeditionReadyToStart(colony);
//            FleetEntity expedition = FleetEntity.createExpeditionFleet(colony);
//            if(colony.canSent(expedition)) {
//                setExpeditionReadyToStart(expedition);
//                failCount = 0;
//            } else {
//                checkColonyStatus(colony);
////                pause = 1;
//            }
            pause = SHORT_PAUSE;
            expeditionQueue.add(colony);
        } else {
            pause = LONG_PAUSE;
        }
        if(failCount > 20) {
            SleepUtil.secondsToSleep(600);
            failCount = 0;
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
