package org.enoch.snark.instance.si;

import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.db.entity.ModuleEntity;
import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.enoch.snark.db.repository.ThreadRepository;
import org.enoch.snark.expression.ExpressionService;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ThreadBeanFactory;
import org.enoch.snark.instance.si.module.DatabaseThreadTypeRegistry;
import org.enoch.snark.instance.si.module.update.UpdateThread;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ThreadManagerTest {

    private final ThreadRepository threadRepository = mock(ThreadRepository.class);
    private final ThreadBeanFactory threadBeanFactory = mock(ThreadBeanFactory.class);
    private final ExpressionService expressionService = mock(ExpressionService.class);

    private ThreadManager manager;
    private ThreadEntity databaseThread;

    @BeforeEach
    void setUp() {
        manager = new ThreadManager(
                threadRepository,
                threadBeanFactory,
                new DatabaseThreadTypeRegistry(),
                expressionService
        );
        ModuleEntity module = new ModuleEntity("database-module");
        module.id = 1L;
        module.time = "on";
        module.conditions = "[]";
        databaseThread = new ThreadEntity("worker", module, "update");
        databaseThread.id = 2L;
        databaseThread.time = "on";
        databaseThread.conditions = "[]";
        databaseThread.updated = LocalDateTime.parse("2026-01-01T10:00:00");

        when(threadRepository.findAllWithModule()).thenAnswer(invocation -> List.of(databaseThread));
        when(expressionService.evaluate(anyString(), anyMap(), eq(Boolean.class))).thenReturn(true);
    }

    @Test
    void keepsUnchangedThreadAndRestartsWhenDatabaseUpdateAdvances() {
        AbstractThread firstInstance = mock(AbstractThread.class);
        AbstractThread secondInstance = mock(AbstractThread.class);
        when(threadBeanFactory.create(UpdateThread.class, databaseThread))
                .thenReturn(firstInstance, secondInstance);

        manager.synchronizeThreads();
        manager.synchronizeThreads();

        verify(threadBeanFactory, times(1)).create(UpdateThread.class, databaseThread);

        databaseThread.updated = databaseThread.updated.plusSeconds(1);
        manager.synchronizeThreads();

        verify(threadBeanFactory, times(2)).create(UpdateThread.class, databaseThread);
        verify(threadBeanFactory).destroy(firstInstance);

        when(threadRepository.findAllWithModule()).thenReturn(List.of());
        manager.synchronizeThreads();
        verify(threadBeanFactory).destroy(secondInstance);
    }

    @Test
    void managedDefinitionsPutThreadControlConfigsAtTheBeginning() {
        AbstractThread runningInstance = mock(AbstractThread.class);
        databaseThread.pause = "30S";
        databaseThread.debug = "true";
        databaseThread.commandLimit = "25";
        databaseThread.conditions = "thread-condition";

        ThreadDefinition threadDefinition = new ThreadDefinition("update", "Update description", List.of(
                new ConfigDefinition("refresh", "12M", false, "Refresh interval")));
        when(runningInstance.threadConfiguration()).thenReturn(threadDefinition);
        when(runningInstance.getThreadEntity()).thenReturn(databaseThread);
        when(threadBeanFactory.create(UpdateThread.class, databaseThread)).thenReturn(runningInstance);

        manager.synchronizeThreads();

        List<ThreadDefinition> definitions = manager.managedDefinitions();
        assertEquals(1, definitions.size());
        List<ConfigDefinition> configs = definitions.getFirst().getConfigs();
        assertEquals(List.of("time", "pause", "debug", "command_limit", "conditions"),
                configs.subList(0, 5).stream().map(ConfigDefinition::getKey).toList());
        assertEquals("30S", configs.get(1).getValue());
        assertEquals("true", configs.get(2).getValue());
        assertEquals("25", configs.get(3).getValue());
        assertEquals("thread-condition", configs.get(4).getValue());
        assertEquals("database-module", definitions.getFirst().getModule());
        assertEquals("worker", definitions.getFirst().getThread());
        assertEquals("refresh", configs.get(5).getKey());
    }

    @Test
    void stopsAnExistingThreadWhenItNoLongerQualifiesToRun() {
        AbstractThread runningInstance = mock(AbstractThread.class);
        when(threadBeanFactory.create(UpdateThread.class, databaseThread)).thenReturn(runningInstance);

        manager.synchronizeThreads();
        when(expressionService.evaluate(eq("[]"), anyMap(), eq(Boolean.class))).thenReturn(false);
        manager.synchronizeThreads();

        verify(threadBeanFactory).destroy(runningInstance);
    }

    @Test
    void initializesThreadIdentityWithoutReadingThreadOrModuleConfigs() {
        AbstractThread runningInstance = mock(AbstractThread.class);
        when(threadBeanFactory.create(UpdateThread.class, databaseThread)).thenReturn(runningInstance);

        new ConfigEntity(databaseThread, "source", "#all_moons()");
        when(threadRepository.findAllWithModule()).thenReturn(List.of(databaseThread));

        manager.synchronizeThreads();

        verify(threadBeanFactory).create(UpdateThread.class, databaseThread);
    }

    @Test
    void doesNotStartThreadWhenModuleScheduleIsOff() {
        databaseThread.module.time = "off";
        when(threadRepository.findAllWithModule()).thenReturn(List.of(databaseThread));

        manager.synchronizeThreads();

        verifyNoInteractions(threadBeanFactory);
    }

    @Test
    void doesNotStartThreadWhenItsScheduleIsOff() {
        databaseThread.time = "off";

        manager.synchronizeThreads();

        verifyNoInteractions(threadBeanFactory);
    }

    @Test
    void moduleConditionStopsAllThreadsInThatModule() {
        AbstractThread runningInstance = mock(AbstractThread.class);
        when(threadBeanFactory.create(UpdateThread.class, databaseThread)).thenReturn(runningInstance);
        manager.synchronizeThreads();

        databaseThread.module.conditions = "module-condition";
        when(expressionService.evaluate(eq("module-condition"), anyMap(), eq(Boolean.class))).thenReturn(false);
        manager.synchronizeThreads();

        verify(threadBeanFactory).destroy(runningInstance);
    }

    @Test
    void threadConditionPreventsStartingThatThread() {
        databaseThread.conditions = "thread-condition";
        when(expressionService.evaluate(eq("thread-condition"), anyMap(), eq(Boolean.class))).thenReturn(false);

        manager.synchronizeThreads();

        verifyNoInteractions(threadBeanFactory);
    }

    @Test
    void threadConditionStopsAnExistingThread() {
        AbstractThread runningInstance = mock(AbstractThread.class);
        when(threadBeanFactory.create(UpdateThread.class, databaseThread)).thenReturn(runningInstance);
        manager.synchronizeThreads();

        databaseThread.conditions = "thread-condition";
        when(expressionService.evaluate(eq("thread-condition"), anyMap(), eq(Boolean.class))).thenReturn(false);
        manager.synchronizeThreads();

        verify(threadBeanFactory).destroy(runningInstance);
    }

}
