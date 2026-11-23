package org.enoch.snark.instance.si.module;

import jakarta.annotation.PostConstruct;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.snark.common.*;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.common.time.TimeScheduler;
import org.enoch.snark.action.command.OpenPageCommand;
import org.enoch.snark.db.entity.ThreadEntity;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.GalaxyRepository;
import org.enoch.snark.expression.ExpressionService;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.service.ConditionChecker;
import org.enoch.snark.instance.si.Core;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;
import static org.enoch.snark.action.command.status.ExecutionIssue.STUCK_IN_QUEUE_IN_PROGRESS_AS_WAITING;
import static org.enoch.snark.action.command.status.ExecutionStatus.*;
import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;
import static org.enoch.snark.instance.si.module.ThreadMap.*;

/**
 * Base class for all worker threads. Threads run in a loop that checks conditions, generates
 * commands via onStep(), and pushes them to the core queue. For thread lifecycle, configuration
 * resolution, and the module grouping pattern, see:
 * doc/architecture/Thread system.md
 */
@NoArgsConstructor(force = true)
@Slf4j
public abstract class AbstractThread extends ExecutorImpl {

    public static final long NO_LIMIT = -1L;
    public static final String PROCESSING_SUFFIX = "_processing";
    public static final String DONE_PROCESSING = "DONE";
    public static final String IN_PROGRESS_PROCESSING = "IN_PROGRESS";

    @Autowired
    protected final Core core;
    @Autowired
    protected final CacheEntryRepository cacheEntryRepository;
    @Autowired
    protected final ColonyRepository colonyRepository;
    @Autowired
    protected final GalaxyRepository galaxyRepository;
    @Autowired
    protected final ExpressionService expressionService;

    private RunningProcessor runningProcessor = new RunningProcessor();

    @Getter
    @Setter
    protected ThreadEntity threadEntity;

    protected Duration pause = new Duration("1M");
    private volatile boolean isLive = true;
    protected Long limit = NO_LIMIT;
    protected String currentStateDescription = "UNKNOWN";

    protected CommandContainer container = new CommandContainer();
    private final Map<String, String> notification = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        runningProcessor = new RunningProcessor();
    }

    protected void onStart() {
//        try {
//            if(limit > 0) saveProcessingStatus(IN_PROGRESS_PROCESSING);
//            if(map.containsKey(SOURCE)) {
//                expressionService.evaluateCoordinates(map.getConfig(SOURCE)).stream()
//                                .map(PlanetData::getColony)
//                                .forEach(colony -> {
//                    if(DateUtil.isExpired2H(colony.updated))
//                        core.push(new OpenPageCommand(FLEETDISPATCH, colony).sourceHash(this.getClass().getSimpleName()));
//                });
//            }
//        } catch (Throwable throwable) {
//            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
//            System.err.println("For "+this.threadEntity.name+" ["+time+"] onStart skipping");
//            throwable.printStackTrace();
//        }
    }

    protected abstract void onStep();

    @Override
    public void run() {
        onStart();
        while(isLive) {
            try {
                if(shouldWaitForDeque() ) {
                    SleepUtil.sleep();
                    continue;
                }
                limit = NumberUtil.toLong(threadEntity.commandLimit);
                pause.update(threadEntity.pause); // default "10S"
                 onStep();
                 if(container.anyNotProcessed())  limitedPush();
                 SleepUtil.sleep(pause);
                cacheProcessingStatus();
            } catch (Exception e) {
                runningProcessor.logChangedStatus("Thread " + threadEntity.name, threadEntity);
                String message = Objects.toString(e.getMessage(), "");
                if (!notification.containsKey(message)) {
                    String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    StringWriter stackTrace = new StringWriter();
                    e.printStackTrace(new PrintWriter(stackTrace));
                    String details = "Thread " + threadEntity.name + " [" + time + "]" +
                            System.lineSeparator() + stackTrace;
                    notification.put(message, details);
                    log.warn(notification.get(message));
                }
            }
        }
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.err.println("Destroy "+threadEntity.name+" ["+time+"]");
    }

    private void cacheProcessingStatus() {
        if(limit > 0) if(container.isEmpty()) saveProcessingStatus(DONE_PROCESSING);
        else if(container.anyNotProcessed()) saveProcessingStatus(IN_PROGRESS_PROCESSING);
        else saveProcessingStatus(DONE_PROCESSING);
    }

    protected void saveProcessingStatus(String statusName) {
        String key = threadEntity.name + PROCESSING_SUFFIX;
        String oldValue = cacheEntryRepository.getValue(key);
        if(statusName!= null && !statusName.equals(oldValue)) {
            cacheEntryRepository.setValue(key, statusName);
            log("SET " + key + " = " + statusName + " " + container);
        }
    }

    protected boolean shouldWaitForDeque() {
        return !core.isDequeReady();
    }

    protected String getThreadConfig(String key, String defaultValue) {
        String value = switch (key) {
            case TIME -> threadEntity.time;
            case PAUSE -> threadEntity.pause;
            case DEBUG -> threadEntity.debug;
            case COMMAND_LIMIT -> threadEntity.commandLimit;
            case CONDITIONS -> threadEntity.conditions;
            case NAME -> threadEntity.name;
            case TYPE -> threadEntity.type;
            default -> null;
        };
        return StringUtils.isBlank(value) ? defaultValue : value;
    }

    public RunningState getActualState() {
        return runningProcessor.getActualState();
    }

    public void destroy() {
        isLive = false;
    }

    public ThreadDefinition threadDefinition() {
        throw new UnsupportedOperationException("Thread definition is not provided by " + getClass().getName());
    }

    protected void log(String message) {
        Debug.log(threadEntity, message);
    }

    protected void logList(String header, Iterable list) {
        if(Boolean.getBoolean(threadEntity.debug)) {
            StringBuilder stringBuilder = new StringBuilder(header + ": ");
            list.forEach(command -> stringBuilder.append(command).append(", "));
            log(stringBuilder.toString());
        }
    }

    public Map<String, String> getNotifications() {
        return Map.copyOf(notification);
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
                        System.err.println("For limitedPush "+threadEntity.name+" ["+time+"] core.push do not put in queue command "+command.hash());
                        command.getStatus().setStatus(CRASHED);
                        command.getStatus().setIssue(STUCK_IN_QUEUE_IN_PROGRESS_AS_WAITING);
                    });
            container.updateMap(WAITING);
            logList(container.toString(), pooledList);
        }
    }

    protected void setCurrentStateDescription(String state) {
        if(!state.equals(currentStateDescription)) {
            log("Thread " + threadEntity.name + " state description: " + state);
            this.currentStateDescription = state;
        }
    }

    public abstract ThreadDefinition threadConfiguration();

    @Override
    public String toString() {
        return threadEntity.name+":"+runningProcessor.getActualState();
    }
}
