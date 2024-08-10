package org.enoch.snark.instance.si.module;

import org.enoch.snark.common.*;
import org.enoch.snark.db.dao.CacheEntryDAO;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.instance.commander.Commander;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.si.module.building.BuildingThread;
import org.enoch.snark.instance.si.module.collector.CollectorThread;
import org.enoch.snark.instance.si.module.defense.DefenseThread;
import org.enoch.snark.instance.si.module.expedition.ExpeditionThread;
import org.enoch.snark.instance.si.module.farm.FarmThread;
import org.enoch.snark.instance.si.module.fleet.FleetThread;
import org.enoch.snark.instance.si.module.fleetSave.FleetSaveThread;
import org.enoch.snark.instance.si.module.hunting.HuntingThread;
import org.enoch.snark.instance.si.module.scan.ScanThread;
import org.enoch.snark.instance.si.module.space.SpaceThread;
import org.enoch.snark.instance.si.module.transport.TransportThread;
import org.enoch.snark.instance.si.module.update.UpdateThread;

import static org.enoch.snark.gi.types.UrlComponent.FLEETDISPATCH;
import static org.enoch.snark.instance.si.module.ConfigMap.SOURCE;
import static org.enoch.snark.instance.si.module.ConfigMap.TYPE;

public abstract class AbstractThread extends Thread {

    protected final Instance instance;
    private final RunningProcessor runningProcessor = new RunningProcessor();
    protected final Commander commander;
    protected final CacheEntryDAO cacheEntryDAO;
    protected final FleetDAO fleetDAO;
    protected final TargetDAO targetDAO;

    protected int pause = 1;
    protected ConfigMap map;
    private boolean isLive = true;
    protected Boolean debug;

    public static AbstractThread create(ConfigMap map) {
        if(map.name().contains(UpdateThread.threadType)) return new UpdateThread(map);
        else if(map.name().contains(DefenseThread.threadType)) return new DefenseThread(map);
        else if(map.name().contains(FleetSaveThread.threadType)) return new FleetSaveThread(map);
        else if(map.name().contains(ExpeditionThread.threadType)) return new ExpeditionThread(map);
        else if(map.name().contains(BuildingThread.threadType)) return new BuildingThread(map);
        else if(map.name().contains(SpaceThread.threadType)) return new SpaceThread(map);
        else if(map.name().contains(ScanThread.threadType)) return new ScanThread(map);
        else if(map.name().contains(FarmThread.threadType)) return new FarmThread(map);
        else if(map.name().contains(CollectorThread.threadType)) return new CollectorThread(map);
        else if(map.name().contains(TransportThread.threadType)) return new TransportThread(map);
        else if(map.name().contains(HuntingThread.threadType)) return new HuntingThread(map);
        else return new FleetThread(map);
    }

    public AbstractThread(ConfigMap map) {
        updateMap(map);
        instance = Instance.getInstance();
        commander = Commander.getInstance();
        fleetDAO = FleetDAO.getInstance();
        targetDAO = TargetDAO.getInstance();
        cacheEntryDAO = CacheEntryDAO.getInstance();
        setName(map.name());
    }

    protected abstract String getThreadType();

    protected abstract int getPauseInSeconds();

    protected void onStart() {
        if(map.containsKey(SOURCE)) {
            ColonyDAO.getInstance().getColonies(map.getConfig(SOURCE)).forEach(colony -> {
                if(DateUtil.isExpired2H(colony.updated))
                    new OpenPageCommand(FLEETDISPATCH, colony).push();
            });
        }
    }

    protected abstract void onStep();

    @Override
    public void run() {
        super.run();
        while(isLive) {
            RunningState actualState = runningProcessor.update(map.isOn(), pauseProcessing())
                    .logChangedStatus("Thread " + map.name(), map)
                    .getActualState();
            if(RunningState.STARTING.equals(actualState)) onStart();

            if (RunningState.isRunning(actualState)) {
                try {
                    debug = map.getConfigBoolean(ConfigMap.DEBUG, false);
                    onStep();
                } catch (Exception e) {
                    runningProcessor.logChangedStatus("Thread " + map.name(), map);
                    e.printStackTrace();
                }
                SleepUtil.secondsToSleep(Long.valueOf(getPause()));
            }
            else SleepUtil.secondsToSleep(60L);
        }
        System.err.println("Destroy "+map.name());
    }

    protected boolean pauseProcessing() {
        return !commander.isRunning();
    }

    private int getPause() {
        return map.getConfigInteger(ConfigMap.PAUSE, getPauseInSeconds());
    }

    public void updateMap(ConfigMap map) {
        map.put(TYPE, getThreadType());
        this.map = map;
    }

    public RunningState getActualState() {
        return runningProcessor.getActualState();
    }

    public void destroy() {
        isLive = false;
    }

    public int getRequestedFleetCount() {
        return 0;
    }

    protected void log(String message) {
        Debug.log(map.getConfig(ConfigMap.NAME), message);
    }
}
