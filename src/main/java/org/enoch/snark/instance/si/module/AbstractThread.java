package org.enoch.snark.instance.si.module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import jakarta.annotation.PostConstruct;
import lombok.*;
import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.snark.common.*;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.common.time.TimeScheduler;
import org.enoch.snark.db.dao.CacheEntryDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.action.command.OpenPageCommand;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.to.PlanetData;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.service.PlanetService;
import org.enoch.snark.instance.si.Core;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.enoch.snark.action.command.status.ExecutionStatus.*;
import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;
import static org.enoch.snark.instance.si.module.ThreadMap.*;

@NoArgsConstructor(force = true)
public abstract class AbstractThread extends ExecutorImpl {

    public static final long NO_LIMIT = -1L;
    @Autowired
    protected final Core core;
    @Autowired
    protected final ColonyRepository colonyRepository;
    @Autowired
    protected final PlanetService planetService;
    @Autowired
    private ObjectMapper objectMapper;

    private RunningProcessor runningProcessor = new RunningProcessor();
    protected  CacheEntryDAO cacheEntryDAO;
    protected  FleetDAO fleetDAO;
    protected  TargetDAO targetDAO;

    @Setter
    protected AbstractModule module;

    protected ThreadMap map;
    private TimeScheduler timeScheduler;
    protected Duration pause = new Duration("1M");
    private boolean isLive = true;
    protected Boolean debug;
    protected Long limit = NO_LIMIT;

    protected List<AbstractCommand> commands = new ArrayList<>();
    protected ListMultimap<String, AbstractCommand> commandsMap = ArrayListMultimap.create();

    @PostConstruct
    public void init() {
        runningProcessor = new RunningProcessor();
        timeScheduler = new TimeScheduler(OFF);
    }

    protected String getThreadType() {return "";}

    protected String defaultPause() {return "1S";}

    protected void onStart() {
        try {
            if(map.containsKey(SOURCE)) {
                colonyRepository.findByCode(map.getConfig(SOURCE)).forEach(colony -> {
                    if(DateUtil.isExpired2H(colony.updated))
                        core.push(new OpenPageCommand(FLEETDISPATCH, colony).sourceHash(this.getClass().getSimpleName()));
                });
            }
        } catch (Throwable throwable) {
            System.err.println("For "+this.map.get("name")+" onStart skipping");
            throwable.printStackTrace();
        }
    }

    protected abstract void onStep();

    @Override
    public void run() {
        while(isLive) {
            if(shouldWaitForDeque()) continue;
            RunningState actualState = runningProcessor.update(timeScheduler.isOn(), module.getTimeScheduler().isOn())
                    .logChangedStatus("Thread " + map.name(), timeScheduler, " ", timeScheduler, " ", map)
                    .getActualState();
            if(RunningState.STARTING.equals(actualState)) onStart();

            if (RunningState.isRunning(actualState)) {
                try {
                    debug = map.getConfigBoolean(ThreadMap.DEBUG, false);
                    limit = map.getConfigNumber(ThreadMap.COMMAND_LIMIT, "-1");
                    updatePause();
//                    log(actualState.name()+" pause="+pause);

                    onStep();
                    limitedPush();
                } catch (Exception e) {
                    runningProcessor.logChangedStatus("Thread " + map.name(), map);
                    e.printStackTrace();
                }
                SleepUtil.sleep(pause);
            }
            else SleepUtil.sleep(pause);
        }
        System.err.println("Destroy "+map.name());
    }

    protected boolean shouldWaitForDeque() {
        return !core.isDequeReady();
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

    public ThreadMap map() {
        return map;
    }

    protected boolean readyToPush(String key) {
        if(!commandsMap.containsKey(key)) return true;
        List<AbstractCommand> commandsChain = commandsMap.get(key);
        if(commandsChain.isEmpty()) return true;
        return commandsChain.stream().allMatch(AbstractCommand::executed);
    }

    protected void pushCommands(AbstractCommand command) {
        pushCommand(command.getHash(), command);
    }

    protected void pushCommand(String key, AbstractCommand command) {
        if(command == null) return;
        command.getStatus().setStatus(WAITING);
        putCommandChain(key, command);

        if(NO_LIMIT == limit) {
            core.push(command);
        }
    }

    private void putCommandChain(String key, AbstractCommand command) {
        commandsMap.removeAll(key);
        while(command != null) {
            commandsMap.put(key, command);
            if(command.isFollowingAction()) {
                command = command.getFollowingAction().getCommand();
            } else break;
        }
    }

    private void limitedPush() {
        if(limit < 0) return;

        long inQueueCount = commandsMap.asMap().values().stream()
                .filter(list -> !list.isEmpty() && isFirstCommandInQueue(list))
                .count();
        while(inQueueCount <= limit) {
            Optional<Collection<AbstractCommand>> firstWaitingList = commandsMap.asMap().values().stream()
                    .filter(list -> !list.isEmpty() && isFirstCommandWaiting(list))
                    .findFirst();
            if(firstWaitingList.isEmpty()) break;
            AbstractCommand toQueue = firstWaitingList.get().stream().findFirst().get();
            core.push(toQueue);
            inQueueCount++;
        }
    }

    private boolean isFirstCommandInQueue(Collection<AbstractCommand> cmd) {
        AbstractCommand first = cmd.stream().findFirst().get();
        return asList(NEW, IN_PROGRESS).contains(first);
    }

    private boolean isFirstCommandWaiting(Collection<AbstractCommand> cmd) {
        AbstractCommand first = cmd.stream().findFirst().get();
        return asList(WAITING).contains(first);
    }

    protected void pushOldCommand(AbstractCommand command) {
        pushOldCommands(Collections.singletonList(command));
    }

    protected void pushOldCommands(List<AbstractCommand> commands) {
        if(commands != null) this.commands = commands;
        else this.commands = new ArrayList<>();
        this.commands.forEach(command -> core.push(command));
    }

    protected void noDuplicationPushCommands(List<AbstractCommand> commands) {
        if(commands == null) return;
        List<AbstractCommand> notExecuted = this.commands.stream()
                .filter(AbstractCommand::notExecuted).toList();

        commands.stream()
                .filter(command -> notExecuted.stream().noneMatch(notExecutedCommand -> notExecutedCommand.getHash().equals(command.getHash())))
                .forEach(command -> core.push(command));
    }

    protected boolean waitingForExecution() {
        if(commands.isEmpty()) return false;
        return commands.stream()
                .anyMatch(AbstractCommand::notExecuted);
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
        List<PlanetData> planetData = planetService.fromExpression(sourcesCode);
        return planetData.stream().map(PlanetData::getColony).collect(Collectors.toList());
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
