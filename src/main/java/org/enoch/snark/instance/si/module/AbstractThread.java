package org.enoch.snark.instance.si.module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.snark.common.*;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.common.time.TimeScheduler;
import org.enoch.snark.action.command.OpenPageCommand;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.GalaxyRepository;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.to.PlanetData;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.service.ConditionChecker;
import org.enoch.snark.instance.service.CoordinateExpressionService;
import org.enoch.snark.instance.si.Core;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.enoch.snark.action.command.status.ExecutionIssue.STUCK_IN_QUEUE_IN_PROGRESS_AS_WAITING;
import static org.enoch.snark.action.command.status.ExecutionStatus.*;
import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;
import static org.enoch.snark.instance.si.module.ThreadMap.*;

@NoArgsConstructor(force = true)
public abstract class AbstractThread extends ExecutorImpl {

    public static final long NO_LIMIT = -1L;
    public static final String PROCESSING_SUFFIX = "_processing";
    public static final String DONE_PROCESSING = "DONE";
    public static final String IN_PROGRESS_PROCESSING = "IN_PROGRESS";

    @Autowired
    protected final Core core;
    @Autowired
    protected final ConditionChecker conditionChecker;
    @Autowired
    protected final CacheEntryRepository cacheEntryRepository;
    @Autowired
    protected final ColonyRepository colonyRepository;
    @Autowired
    protected final GalaxyRepository galaxyRepository;
    @Autowired
    protected final CoordinateExpressionService coordinateExpressionService;
    @Autowired
    private ObjectMapper objectMapper;

    private RunningProcessor runningProcessor = new RunningProcessor();

    @Setter
    protected AbstractModule module;

    protected ThreadMap map;
    private TimeScheduler timeScheduler;
    protected Duration pause = new Duration("1M");
    private boolean isLive = true;
    protected Boolean debug;
    protected Long limit = NO_LIMIT;

    protected CommandContainer container = new CommandContainer();

    @PostConstruct
    public void init() {
        runningProcessor = new RunningProcessor();
        timeScheduler = new TimeScheduler(OFF);
    }

    protected String getThreadType() {return "";}

    protected String defaultPause() {return "10S";}

    protected void onStart() {
        try {
            if(limit > 0) saveProcessingStatus(IN_PROGRESS_PROCESSING);
            if(map.containsKey(SOURCE)) {
                coordinateExpressionService.fromExpression(map.getConfig(SOURCE)).stream()
                                .map(PlanetData::getColony)
                                .forEach(colony -> {
                    if(DateUtil.isExpired2H(colony.updated))
                        core.push(new OpenPageCommand(FLEETDISPATCH, colony).sourceHash(this.getClass().getSimpleName()));
                });
            }
        } catch (Throwable throwable) {
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            System.err.println("For "+this.map.get("name")+" ["+time+"] onStart skipping");
            throwable.printStackTrace();
        }
    }

    protected abstract void onStep();

    @Override
    public void run() {
        while(isLive) {
            try {
                if(shouldWaitForDeque() || shouldWaitForConditions()) {
                    SleepUtil.sleep();
                    continue;
                }

                RunningState actualState = runningProcessor.update(timeScheduler.isOn(), module.getTimeScheduler().isOn())
                        .logChangedStatus("Thread " + map.name(), timeScheduler, " ", timeScheduler, " ", map)
                        .getActualState();

                if(RunningState.STARTING.equals(actualState)) onStart();

                if (RunningState.isRunning(actualState)) {
                    debug = map.getConfigBoolean(ThreadMap.DEBUG, false);
                    limit = map.getConfigNumber(ThreadMap.COMMAND_LIMIT, "-1");
                    updatePause();
//                    log(actualState.name()+" pause="+pause);

//                    if(!container.anyNotProcessed())
                        onStep();
                    if(container.anyNotProcessed())
                        limitedPush();
                    SleepUtil.sleep(pause);
                }
                else SleepUtil.sleep(pause);
                cacheProcessingStatus();
            } catch (Exception e) {
                runningProcessor.logChangedStatus("Thread " + map.name(), map);
                String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                System.err.println(map.name()+" ["+time+"]");
                e.printStackTrace();
            }
        }
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.err.println("Destroy "+map.name()+" ["+time+"]");
    }

    private void cacheProcessingStatus() {
        if(limit > 0) if(container.isEmpty()) saveProcessingStatus(DONE_PROCESSING);
        else if(container.anyNotProcessed()) saveProcessingStatus(IN_PROGRESS_PROCESSING);
        else saveProcessingStatus(DONE_PROCESSING);
    }

    protected void saveProcessingStatus(String statusName) {
        String key = map().name() + PROCESSING_SUFFIX;
        String oldValue = cacheEntryRepository.getValue(key);
        if(statusName!= null && !statusName.equals(oldValue)) {
            cacheEntryRepository.setValue(key, statusName);
            log("SET " + key + " = " + statusName + " " + container);
        }
    }

    protected boolean shouldWaitForDeque() {
        return !core.isDequeReady();
    }

    private boolean shouldWaitForConditions() {
        return !conditionChecker.fit(getConditions(CONDITIONS), null);
    }

    private boolean isOn() {
        return timeScheduler.isOn() && module.getTimeScheduler().isOn();
    }

    private void updatePause() {
        pause.update(map.getConfig(ThreadMap.PAUSE, defaultPause()));
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
        Debug.log(this, message);
    }

    protected void logList(String header, Iterable list) {
        if(map().getConfigBoolean(DEBUG, false)) {
            StringBuilder stringBuilder = new StringBuilder(header + ": ");
            list.forEach(command -> stringBuilder.append(command).append(", "));
            log(stringBuilder.toString());
        }
    }

    public ThreadMap map() {
        return map;
    }

    protected boolean alreadyWaiting(String key) {
        return container.anyNotProcessed(key);
    }

    private boolean anythingNotExecuted(Collection<AbstractCommand> commandsChain) {
        if(commandsChain.isEmpty()) return false;
        return commandsChain.stream().anyMatch(AbstractCommand::notExecuted);
    }

    protected void pushCommand(AbstractCommand command) {
        pushCommand(command.getHash(), command);
    }

    protected void pushCommand(String key, AbstractCommand command) {
        container.pushCommand(key, command);
    }

    protected void refreshSingleListCommand(AbstractCommand command) {
        refreshCommand(MAIN, command);
    }

    protected void refreshCommand(AbstractCommand command) {
        refreshCommand(command.hash(), command);
    }

    protected void refreshCommand(String key, AbstractCommand command) {
        if(container.anyNotProcessed(key)) return;
        if(container.contains(key)) container.removeKey(key);
        container.pushCommand(key, command);
    }

    private void limitedPush() {
        container.updateMap(IN_PROGRESS);
        long elementsCountToPool = limit == NO_LIMIT ? container.incomingCount() : limit - container.inProgressCount();
        if(elementsCountToPool > 0 && container.incomingCount()>0) {
            List<AbstractCommand> pooledList = container.pool(elementsCountToPool);
            pooledList.forEach(command -> core.push(command));
            pooledList.stream()
                    .filter(command -> WAITING.equals(command.getStatus().getStatus()))
                    .forEach(command -> {
                        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        System.err.println("For limitedPush "+map().name()+" ["+time+"] core.push do not put in queue command "+command.hash());
                        command.getStatus().setStatus(CRASHED);
                        command.getStatus().setIssue(STUCK_IN_QUEUE_IN_PROGRESS_AS_WAITING);
                    });
            container.updateMap(WAITING);
            logList(container.toString(), pooledList);
        }
    }

    private boolean isAnyCommandInQueue(Collection<AbstractCommand> cmd) {
        return cmd.stream().anyMatch(command -> asList(NEW, IN_PROGRESS).contains(command.getStatus().getStatus()));
    }

    private boolean isWaiting(Collection<AbstractCommand> cmd) {
        return cmd.stream().allMatch(command -> asList(WAITING).contains(command.getStatus().getStatus()));
//        AbstractCommand first = cmd.stream().findFirst().get();
//        return asList(WAITING).contains(first);
    }

    protected boolean isNearestConfig(String key) {
        return (map().containsKey(key) &&  !StringUtils.EMPTY.equals(map().get(key))) ||
                (module.getModuleMap().get(MAIN).containsKey(key) && !StringUtils.EMPTY.equals(module.getModuleMap().get(MAIN).get(key)));
    }

    protected String getNearestConfig(String key, String defaultValue) {
        if(map().containsKey(key)) return map().get(key);
        ThreadMap moduleMainMap = module.getModuleMap().get(MAIN);
        if(moduleMainMap.containsKey(key)) return moduleMainMap.get(key);
        return defaultValue;
    }

    public Resources getNearestLeaveResources(ColonyType type, Resources defaultResources) {
        String key = ColonyType.PLANET.equals(type) ? LEAVE_PLANET_RESOURCES : LEAVE_MOON_RESOURCES;
        String nearestConfig = getNearestConfig(key, null);
        if (nearestConfig == null) return defaultResources;
        return Resources.parse(nearestConfig);
    }

    protected List<ColonyEntity> getSources(String defaultValue) {
        String sourcesCode = map().getSourcesCode(defaultValue);
        List<PlanetData> planetData = coordinateExpressionService.fromExpression(sourcesCode);
        return planetData.stream().map(PlanetData::getColony).collect(Collectors.toList());
    }

    protected List<PlanetData> getNearestCoordinate(String defaultValue) {
        String coordinateCode = getNearestConfig(COORDINATE, defaultValue);
        return  coordinateExpressionService.fromExpression(coordinateCode);
    }

    protected List<AbstractCondition> getConditions(String configName) {
        String configJson = map().getConfig(configName, "[]");
        try {
            return objectMapper.readValue(configJson, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
