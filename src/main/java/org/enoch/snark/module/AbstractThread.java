package org.enoch.snark.module;

import org.enoch.snark.common.RunningStatus;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.CacheEntryDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.instance.Commander;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.config.Config;

import java.util.logging.Logger;

import static org.enoch.snark.instance.config.Config.PAUSE;

public abstract class AbstractThread extends Thread {

    private static final Logger log = Logger.getLogger(AbstractThread.class.getName());
    protected final Instance instance;
    private boolean isRunning = false;
    protected final Commander commander;
    protected final CacheEntryDAO cacheEntryDAO;
    protected final FleetDAO fleetDAO;
    protected final TargetDAO targetDAO;

    protected int pause = 1;

    public AbstractThread() {
        instance = Instance.getInstance();
        commander = Commander.getInstance();
        fleetDAO = FleetDAO.getInstance();
        targetDAO = TargetDAO.getInstance();
        cacheEntryDAO = CacheEntryDAO.getInstance();
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
            RunningStatus runningStatus = new RunningStatus(isRunning, shouldRunning());
            runningStatus.log("Thread " + getThreadName());
            isRunning = runningStatus.shouldRunning();
            if (isRunning) {
                try {
                    onStep();
                } catch (Exception e) {
                    System.err.println(getThreadName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
                SleepUtil.secondsToSleep(getPause());
            }
            else SleepUtil.secondsToSleep(60);
        }
    }

    private int getPause() {
        return Instance.config.getConfigInteger(getThreadName(), PAUSE, getPauseInSeconds());
    }

    private boolean shouldRunning() {
        if(!commander.isRunning()) return false;
        return Instance.config.isOn(getThreadName());
    }

    public boolean isRunning() {
        return isRunning;
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
