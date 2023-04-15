package org.enoch.snark.module;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.common.RunningStatus;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.instance.Commander;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.module.expedition.ExpeditionThread;
import org.enoch.snark.module.space.SpaceThread;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

import static org.enoch.snark.instance.config.Config.MAIN;
import static org.enoch.snark.instance.config.Config.MODE;

public abstract class AbstractThread extends Thread {

    private static final Logger log = Logger.getLogger(AbstractThread.class.getName());
    protected final Instance instance;
    private boolean isRunning = false;
    private boolean isAutoRunning = false;
    protected final Commander commander;
    protected final FleetDAO fleetDAO;
    protected final TargetDAO targetDAO;

    protected int pause = 1;

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
            RunningStatus runningStatus = new RunningStatus(isRunning, shouldRunning());
            runningStatus.log("Thread " + getThreadName());
//            if(!commander.isRunning()) continue;
//            if (!isAutoRunning) {
//                boolean isModeOn = Instance.config.isOn(getThreadName());
//                if ((!isRunning && isModeOn) || (isRunning && !isModeOn)) {
//                    System.err.println("Thread " + getThreadName() + (isRunning ? " stop" : " start"));
//                    isRunning = !isRunning;
//                }
//            }


//            boolean shouldRunning = isAutoRunning || isRunning;
            isRunning = runningStatus.shouldRunning();
            if (isRunning) {
                try {
                    onStep();
                } catch (Exception e) {
                    System.err.println(getThreadName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            SleepUtil.secondsToSleep(getPauseInSeconds());
        }
    }

    private boolean shouldRunning() {
        if(!commander.isRunning()) return false;
        if(isAutoRunning) return true;
        return Instance.config.isOn(getThreadName());
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setAutoRunning(boolean running) {
        isAutoRunning = running;
        isRunning = true;
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
