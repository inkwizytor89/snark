package org.enoch.snark.module;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.instance.Commander;
import org.enoch.snark.instance.Instance;

import java.util.logging.Logger;

public abstract class AbstractThread extends Thread {

    private static final Logger log = Logger.getLogger(AbstractThread.class.getName());
    protected final Instance instance;
    private boolean isRunning = false;
    protected final Commander commander;
    protected final FleetDAO fleetDAO;
    protected final TargetDAO targetDAO;

    protected int pause = 0;

    public AbstractThread() {
        instance = Instance.getInstance();
        commander = Commander.getInstance();
        fleetDAO = FleetDAO.getInstance();
        targetDAO = TargetDAO.getInstance();
        setName(this.getClass().getName());
    }

    public abstract String getThreadName();

    protected abstract int getPauseInSeconds();

    protected void onStart() {
    }

    protected abstract void onStep();

    @Override
    public void run() {
        super.run();
        onStart();

        while(true) {
            SleepUtil.secondsToSleep(getPauseInSeconds());
            boolean shouldRunning = isRunning && commander.isRunning();
            if (shouldRunning) {
                try {
                    onStep();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
    public int getRequestedFleetCount() {
        return 0;
    }

    protected boolean noWaitingElementsByTag(String tag) {
        return commander.peekQueues().stream().noneMatch(command -> command.getTags().contains(tag));
    }

    protected boolean noWaitingElements() {
        return noWaitingElementsOnQueues() && noWaitingElementsForProcess();
    }

    protected boolean noWaitingElementsOnQueues() {
        return commander.peekQueues().isEmpty();
    }

    protected boolean noWaitingElementsForProcess() {
        return fleetDAO.findToProcess().isEmpty();
    }
}
