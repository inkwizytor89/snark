package org.enoch.snark.module;

import org.enoch.snark.common.RunningStatus;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.CacheEntryDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.instance.Commander;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.Navigator;
import org.enoch.snark.module.building.BuildingThread;
import org.enoch.snark.module.building.FormsThread;
import org.enoch.snark.module.collector.CollectorThread;
import org.enoch.snark.module.defense.DefenseThread;
import org.enoch.snark.module.expedition.ExpeditionThread;
import org.enoch.snark.module.farm.FarmThread;
import org.enoch.snark.module.fleetSave.FleetSaveThread;
import org.enoch.snark.module.hunting.HuntingThread;
import org.enoch.snark.module.scan.ScanThread;
import org.enoch.snark.module.space.SpaceThread;
import org.enoch.snark.module.transport.TransportThread;
import org.enoch.snark.module.update.UpdateThread;

import java.util.logging.Logger;

public abstract class AbstractThread extends Thread {

    private static final Logger log = Logger.getLogger(AbstractThread.class.getName());
    protected final Instance instance;
    private boolean isRunning = false;
    protected final Commander commander;
    protected final CacheEntryDAO cacheEntryDAO;
    protected final FleetDAO fleetDAO;
    protected final TargetDAO targetDAO;

    protected int pause = 1;
    protected ConfigMap map;

    public AbstractThread(ConfigMap map) {
        if(map != null) {
            this.map = map;
            this.map.put(ConfigMap.TYPE, this.getThreadName());
        }
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
            runningStatus.log("Thread " + map.name());
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
        return map.getConfigInteger(ConfigMap.PAUSE, getPauseInSeconds());
    }

    private boolean shouldRunning() {
        if(!commander.isRunning()) return false;
        if(!map.containsKey(ConfigMap.TIME)) return false;
        return map.isOn();
    }

    public void updateMap(ConfigMap map) {
        this.map = map;
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

    protected boolean stillWaitingForFleet() {
        long fsCount = Navigator.getInstance().getEventFleetList().stream()
                .filter(eventFleet -> Mission.STATIONED.equals(eventFleet.mission) || Mission.COLONIZATION.equals(eventFleet.mission))
                .count();
        int flyPointsSize = instance.getFlyPoints().size();
        return fsCount > flyPointsSize/2;
    }

    protected boolean isAny(ShipEnum... ships) {
        return true;
    }

    public static AbstractThread create(ConfigMap map) {
        if(map.name().contains(UpdateThread.threadName)) return new UpdateThread(map);
        else if(map.name().contains(DefenseThread.threadName)) return new DefenseThread(map);
        else if(map.name().contains(FleetSaveThread.threadName)) return new FleetSaveThread(map);
        else if(map.name().contains(ExpeditionThread.threadName)) return new ExpeditionThread(map);
        else if(map.name().contains(BuildingThread.threadName)) return new BuildingThread(map);
        else if(map.name().contains(FormsThread.threadName)) return new FormsThread(map);
        else if(map.name().contains(SpaceThread.threadName)) return new SpaceThread(map);
        else if(map.name().contains(ScanThread.threadName)) return new ScanThread(map);
        else if(map.name().contains(FarmThread.threadName)) return new FarmThread(map);
        else if(map.name().contains(CollectorThread.threadName)) return new CollectorThread(map);
        else if(map.name().contains(TransportThread.threadName)) return new TransportThread(map);
        else if(map.name().contains(HuntingThread.threadName)) return new HuntingThread(map);
        else throw new RuntimeException("Unknown threadName "+map.name());
    }
}
