package org.enoch.snark.instance.si.module;

import jakarta.annotation.PostConstruct;
import lombok.*;
import org.enoch.snark.common.*;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.common.time.TimeScheduler;
import org.enoch.snark.db.dao.CacheEntryDAO;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.action.command.OpenPageCommand;
import org.enoch.snark.instance.si.Core;
import org.springframework.beans.factory.annotation.Autowired;

import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;
import static org.enoch.snark.instance.si.module.ThreadMap.*;

//@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true)
//@AllArgsConstructor
//@RequiredArgsConstructor
public abstract class AbstractThread extends ExecutorImpl {

    @Autowired
    protected final Core core;

    private RunningProcessor runningProcessor = new RunningProcessor();
    protected  CacheEntryDAO cacheEntryDAO;
    protected  FleetDAO fleetDAO;
    protected  TargetDAO targetDAO;

    @Setter
    protected AbstractModule module;

    protected ThreadMap map;
    private TimeScheduler timeScheduler;
    protected Duration pause = new Duration("1S");
    private boolean isLive = true;
    protected Boolean debug;

    @PostConstruct
    public void init() {
        runningProcessor = new RunningProcessor();
        timeScheduler = new TimeScheduler(OFF);
    }

//    public static AbstractThread create(ThreadMap map) {
//        String name = map.name();
//        if(name.contains(Consumer.threadType)) return new Consumer(map);
//        else if(name.contains(UpdateThread.threadType)) return new UpdateThread(map);
//        else if(name.contains(DefenseThread.threadType)) return new DefenseThread(map);
//        else if(name.contains(FleetSaveThread.threadType)) return new FleetSaveThread(map);
//        else if(name.contains(ExpeditionThread.threadType)) return new ExpeditionThread(map);
//        else if(name.contains(BuildingThread.threadType)) return new BuildingThread(map);
//        else if(name.contains(SpaceThread.threadType)) return new SpaceThread(map);
//        else if(name.contains(ScanThread.threadType)) return new ScanThread(map);
//        else if(name.contains(FarmThread.threadType)) return new FarmThread(map);
//        else if(name.contains(CollectorThread.threadType)) return new CollectorThread(map);
//        else if(name.contains(TransportThread.threadType)) return new TransportThread(map);
//        else if(name.contains(HuntingThread.threadType)) return new HuntingThread(map);
//        else return new FleetThread(map);
//    }


//    public AbstractThread(ThreadMap map) {
//        moduleTime = new TimeScheduler(OFF);
//        threadTime = new TimeScheduler(OFF);
//        updateMap(map);
//        instance = Instance.getInstance();
////        consumer = Consumer.getInstance();
//        fleetDAO = FleetDAO.getInstance();
//        targetDAO = TargetDAO.getInstance();
//        cacheEntryDAO = CacheEntryDAO.getInstance();
//    }

    protected String getThreadType() {return "";}

    protected int getPauseInSeconds() {return 0;}

    protected void onStart() {
        if(map.containsKey(SOURCE)) {
            ColonyDAO.getInstance().getColonies(map.getConfig(SOURCE)).forEach(colony -> {
                if(DateUtil.isExpired2H(colony.updated))
                    new OpenPageCommand(FLEETDISPATCH, colony).sourceHash(this.getClass().getSimpleName()).push();
            });
        }
    }

    protected abstract void onStep();

    @Override
    public void run() {
        System.err.println(this.map.get("name")+" runns");
        if(this.map.get("name").equals("consumer")) {
            System.err.println("Consumer live");
        }
        while(isLive) {
            if(shouldWaitForDeque()) continue;
            RunningState actualState = runningProcessor.update(timeScheduler.isOn(), module.getTimeScheduler().isOn())
                    .logChangedStatus("Thread " + map.name(), timeScheduler, " ", timeScheduler, " ", map)
                    .getActualState();
            if(RunningState.STARTING.equals(actualState)) onStart();

            if (RunningState.isRunning(actualState)) {
                try {
                    debug = map.getConfigBoolean(ThreadMap.DEBUG, false);
                    onStep();
                } catch (Exception e) {
                    runningProcessor.logChangedStatus("Thread " + map.name(), map);
                    e.printStackTrace();
                }
                SleepUtil.secondsToSleep(getPause());
            }
            else SleepUtil.secondsToSleep(60L);
        }
        System.err.println("Destroy "+map.name());
    }

    protected boolean shouldWaitForDeque() {
        return !core.isDequeReady();
    }

    private boolean isOn() {
        return timeScheduler.isOn() && module.getTimeScheduler().isOn();
    }

//    protected boolean pauseProcessing() {
//        return !consumer.isRunning();
//    }

    private long getPause() {
        String pauseInSecondsInput = getPauseInSeconds()+"S";
        pause.update(map.getConfig(ThreadMap.PAUSE, pauseInSecondsInput));
        return pause.getValue().getSeconds();
    }

    public void updateMap(ThreadMap map) {
        timeScheduler.update(map.getConfig(TIME, OFF));
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
        Debug.log(map.getConfig(ThreadMap.NAME), message);
    }

    public ThreadMap map() {
        return map;
    }
}
