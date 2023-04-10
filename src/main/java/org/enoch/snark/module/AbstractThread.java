package org.enoch.snark.module;

import org.apache.commons.lang3.StringUtils;
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
    public static final String TIME = "time";
    public static final String ON = "on";
    public static final String OFF = "off";
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
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

            if(!isAutoRunning) {
                boolean isModeOn = isModeOn();
                if ((!isRunning && isModeOn) || (isRunning && !isModeOn)) {
                    System.err.println("Thread " + getThreadName() + (isRunning ? " stop" : " start"));
                    isRunning = !isRunning;
                }
            }

            boolean shouldRunning = (isAutoRunning || isRunning) && commander.isRunning();
            if (shouldRunning) {
                try {
                    onStep();
                } catch (Exception e) {
                    System.err.println(getThreadName()+": "+e.getMessage());
                    e.printStackTrace();
                }
            }
            SleepUtil.secondsToSleep(getPauseInSeconds());
        }
    }

    private boolean isModeOn() {
        String[] configArray = Instance.config.getConfigArray(getThreadName(), TIME);
        if(configArray == null || configArray.length == 0)  return true;
        if(configArray.length == 1 && configArray[0].equals(StringUtils.EMPTY))  return true;
        if(configArray.length == 1 && configArray[0].equals(ON))  return true;
        if(configArray.length == 1 && configArray[0].equals(OFF))  return false;

        for(String configTerm : configArray) {
            String[] vars = configTerm.split("-");
            if (vars.length == 2) {
                LocalTime start = LocalTime.parse(vars[0], dtf);
                LocalTime end = LocalTime.parse(vars[1], dtf);
                return (nowIsInMiddle(start, end) && start.isBefore(end)) ||
                        (!nowIsInMiddle(end, start) && start.isAfter(end));
            } else return false;
        }
        return false;
    }

    private boolean nowIsInMiddle(LocalTime start, LocalTime end) {
        LocalTime now = LocalTime.now();
        return now.isAfter(start) && now.isBefore(end);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setAutoRunning(boolean running) {
        isAutoRunning = running;
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
