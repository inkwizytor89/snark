package org.enoch.snark.instance.si;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enoch.snark.common.time.TimeScheduler;
import org.enoch.snark.db.entity.ModuleEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.enoch.snark.db.repository.ThreadRepository;
import org.enoch.snark.expression.ExpressionService;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.DatabaseThreadBeanFactory;
import org.enoch.snark.instance.si.module.DatabaseThreadTypeRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseThreadManager {

    private static final LocalDateTime EARLIEST_UPDATE = LocalDateTime.MIN;

    private final ThreadRepository threadRepository;
    private final DatabaseThreadBeanFactory threadBeanFactory;
    private final DatabaseThreadTypeRegistry typeRegistry;
    private final ExpressionService expressionService;
    private final Map<String, ManagedThread> managedThreads = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 30_000)
    @Transactional(readOnly = true)
    public synchronized void synchronizeThreads() {
        Map<String, List<ThreadEntity>> threadsMap = fetchThreadsByModule();
        List<ThreadEntity> activeThreads = new ArrayList<>();
        for (Map.Entry<String, List<ThreadEntity>> entry : threadsMap.entrySet()) {
            activeThreads.addAll(extractActiveThreads(entry.getValue()));
        }
        stopInactiveThreads(activeThreads);
        startNewActiveThreads(activeThreads);
        resetChangedThreads(activeThreads);
    }

    private Map<String, List<ThreadEntity>> fetchThreadsByModule() {
        Map<String, List<ThreadEntity>> grouped = new HashMap<>();
        for (ThreadEntity thread : threadRepository.findAllWithModule()) {
            grouped.computeIfAbsent(thread.module.name, ignored -> new ArrayList<>()).add(thread);
        }
        return grouped;
    }

    private List<ThreadEntity> extractActiveThreads(List<ThreadEntity> moduleThreads) {
        ModuleEntity module = moduleThreads.get(0).module;
        boolean shouldBeActive = shouldBeActive(module.time, module.conditions);
        if (!shouldBeActive) return emptyList();

        return moduleThreads.stream()
                .filter(thread -> !isMainThread(thread))
                .filter(thread -> shouldBeActive(thread.time, thread.conditions))
                .toList();


    }

    private boolean shouldBeActive(String time, String conditions) {
        return isTimeOn(time) && expressionService.evaluateCondition(conditions, Map.of());
    }

    private boolean isMainThread(ThreadEntity thread) {
        return "main".equals(thread.name);
    }

    private void stopInactiveThreads(List<ThreadEntity> activeThreads) {
        Set<String> activeThreadIdentities = activeThreads.stream()
                .map(ThreadEntity::getThreadIdentity)
                .collect(java.util.stream.Collectors.toSet());

        managedThreads.keySet().stream()
                .filter(identity -> !activeThreadIdentities.contains(identity))
                .toList()
                .forEach(this::stopThread);
    }

    private void startNewActiveThreads(List<ThreadEntity> activeThreads) {
        for (ThreadEntity thread : activeThreads) {
            String identity = thread.getThreadIdentity();
            if (managedThreads.containsKey(identity)) {
                continue;
            }

            Class<? extends AbstractThread> threadClass = resolveThreadClass(thread);
            if (threadClass == null) {
                continue;
            }

            startThread(thread.getThreadIdentity(), threadClass, thread,
                    normalizeUpdate(thread.module.updated), normalizeUpdate(thread.updated));
        }
    }

    private void resetChangedThreads(List<ThreadEntity> activeThreads) {
        for (ThreadEntity thread : activeThreads) {
            String identity = thread.getThreadIdentity();
            ManagedThread current = managedThreads.get(identity);
            if (current == null) {
                continue;
            }

            LocalDateTime moduleUpdated = normalizeUpdate(thread.module.updated);
            LocalDateTime threadUpdated = normalizeUpdate(thread.updated);
            if (!hasChanged(current, moduleUpdated, threadUpdated)) {
                continue;
            }

            Class<? extends AbstractThread> threadClass = resolveThreadClass(thread);
            if (threadClass == null) {
                stopThread(identity);
                continue;
            }

            stopThread(identity);
            startThread(identity, threadClass, thread, moduleUpdated, threadUpdated);
        }
    }

    private boolean hasChanged(
            ManagedThread current,
            LocalDateTime moduleUpdated,
            LocalDateTime threadUpdated
    ) {
        return moduleUpdated.isAfter(current.moduleUpdated)
                || threadUpdated.isAfter(current.threadUpdated);
    }

    private Class<? extends AbstractThread> resolveThreadClass(ThreadEntity thread) {
        return typeRegistry.resolve(thread.type).orElseGet(() -> {
            log.error("Unsupported database thread type '{}' for {}",
                    thread.type, thread.getThreadIdentity());
            return null;
        });
    }

    private boolean isTimeOn(String time) {
        return TimeScheduler.parse(time).isOn();
    }

    private LocalDateTime normalizeUpdate(LocalDateTime updated) {
        return updated == null ? EARLIEST_UPDATE : updated;
    }

    private void startThread(
            String identity,
            Class<? extends AbstractThread> threadClass,
            ThreadEntity entity,
            LocalDateTime moduleUpdated,
            LocalDateTime threadUpdated
    ) {
        AbstractThread thread = threadBeanFactory.create(threadClass);
        threadBeanFactory.initializeDatabaseThread(thread, entity);
        ExecutorService executor = Executors.newSingleThreadExecutor(task -> {
            Thread worker = new Thread(task, "snark-db-" + identity);
            worker.setDaemon(true);
            return worker;
        });
        executor.submit(thread);
        managedThreads.put(identity, new ManagedThread(thread, executor, moduleUpdated, threadUpdated));
        log.info("Started database thread {}", identity);
    }

    private void stopThread(String identity) {
        ManagedThread managed = managedThreads.remove(identity);
        if (managed == null) {
            return;
        }
        managed.executor.shutdownNow();
        threadBeanFactory.destroy(managed.thread);
        log.info("Stopped database thread {}", identity);
    }

    @jakarta.annotation.PreDestroy
    public synchronized void shutdown() {
        for (String identity : new HashSet<>(managedThreads.keySet())) {
            stopThread(identity);
        }
    }

    private record ManagedThread(
            AbstractThread thread,
            ExecutorService executor,
            LocalDateTime moduleUpdated,
            LocalDateTime threadUpdated
    ) {
    }
}
