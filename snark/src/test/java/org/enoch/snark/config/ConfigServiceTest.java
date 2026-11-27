package org.enoch.snark.config;

import org.enoch.common.config.ConfigEntry;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.db.entity.ModuleEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.enoch.snark.db.repository.ConfigRepository;
import org.enoch.snark.db.repository.ModuleRepository;
import org.enoch.snark.db.repository.ThreadRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConfigServiceTest {

    @Test
    void infersThreadTypeFromSupportedTypeContainedInThreadName() {
        ModuleRepository moduleRepository = mock(ModuleRepository.class);
        ThreadRepository threadRepository = mock(ThreadRepository.class);
        ConfigRepository configRepository = mock(ConfigRepository.class);
        when(moduleRepository.findByName("module")).thenReturn(Optional.empty());
        when(moduleRepository.save(any(ModuleEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(threadRepository.find("module", "expedition_day")).thenReturn(Optional.empty());
        when(threadRepository.save(any(ThreadEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConfigEntry term = new ConfigEntry();
        term.setModule("module");
        term.setThread("expedition_day");
        term.setKey("time");
        term.setValue("on");

        new ConfigService(moduleRepository, threadRepository, configRepository)
                .updateThreads(List.of(term));

        ArgumentCaptor<ThreadEntity> threadCaptor = ArgumentCaptor.forClass(ThreadEntity.class);
        verify(threadRepository, atLeastOnce()).save(threadCaptor.capture());
        assertEquals("expedition", threadCaptor.getAllValues().getLast().type);
    }

    @Test
    void changingThreadModuleUpdatesBothModulesAndThread() {
        ModuleRepository moduleRepository = mock(ModuleRepository.class);
        ThreadRepository threadRepository = mock(ThreadRepository.class);
        ConfigRepository configRepository = mock(ConfigRepository.class);
        ModuleEntity source = new ModuleEntity("source");
        ModuleEntity target = new ModuleEntity("target");
        ThreadEntity thread = new ThreadEntity("fleet", source, "fleet");

        when(threadRepository.find("source", "fleet")).thenReturn(Optional.of(thread));
        when(moduleRepository.findByName("target")).thenReturn(Optional.of(target));
        when(moduleRepository.findByName("source")).thenReturn(Optional.of(source));

        ConfigEntry term = new ConfigEntry();
        term.setModule("source");
        term.setThread("fleet");

        new ConfigService(moduleRepository, threadRepository, configRepository)
                .changeThreadModule(term, "target");

        assertEquals(target, thread.module);
        assertTrue(thread.updated.isAfter(LocalDateTime.MIN));
        assertTrue(source.updated.isAfter(LocalDateTime.MIN));
        assertTrue(target.updated.isAfter(LocalDateTime.MIN));
        verify(moduleRepository, atLeast(2)).save(any(ModuleEntity.class));
        verify(threadRepository).save(thread);
    }

    @Test
    void deletingConfigUpdatesThreadTimestamp() {
        ModuleRepository moduleRepository = mock(ModuleRepository.class);
        ThreadRepository threadRepository = mock(ThreadRepository.class);
        ConfigRepository configRepository = mock(ConfigRepository.class);
        ModuleEntity module = new ModuleEntity("global");
        ThreadEntity thread = new ThreadEntity("queue-simulator", module, "queue-simulator");
        org.enoch.snark.db.entity.ConfigEntity config =
                new org.enoch.snark.db.entity.ConfigEntity(thread, "pause", "10S");

        when(threadRepository.find("global", "queue-simulator")).thenReturn(Optional.of(thread));
        when(configRepository.findByThreadAndKey(thread, "pause")).thenReturn(Optional.of(config));

        ConfigEntry term = new ConfigEntry();
        term.setModule("global");
        term.setThread("queue-simulator");
        term.setKey("pause");

        new ConfigService(moduleRepository, threadRepository, configRepository).deleteConfig(term);

        verify(configRepository).delete(config);
        verify(threadRepository).save(thread);
        assertTrue(thread.updated.isAfter(LocalDateTime.MIN));
    }

    @Test
    void persistsControlConfigAndUpdatesThreadTimestamp() {
        ModuleRepository moduleRepository = mock(ModuleRepository.class);
        ThreadRepository threadRepository = mock(ThreadRepository.class);
        ConfigRepository configRepository = mock(ConfigRepository.class);
        ModuleEntity module = new ModuleEntity("global");
        ThreadEntity thread = new ThreadEntity("queue-simulator", module, "queue-simulator");
        LocalDateTime previousUpdate = thread.updated;
        when(threadRepository.find("global", "queue-simulator")).thenReturn(Optional.of(thread));

        ConfigEntry term = new ConfigEntry();
        term.setModule("global");
        term.setThread("queue-simulator");
        term.setKey("pause");
        term.setValue("5S");

        new ConfigService(moduleRepository, threadRepository, configRepository)
                .updateThreads(List.of(term));

        ArgumentCaptor<ThreadEntity> threadCaptor = ArgumentCaptor.forClass(ThreadEntity.class);
        verify(threadRepository).save(threadCaptor.capture());
        verify(configRepository, never()).saveAll(anyList());
        assertEquals("5S", threadCaptor.getValue().pause);
        assertTrue(threadCaptor.getValue().updated.isAfter(previousUpdate)
                || threadCaptor.getValue().updated.isEqual(previousUpdate));
    }

    @Test
    void parsesThreePartPropertiesWithCompleteKeyAndValue() {
        ConfigEntry term = ConfigEntry.parse("expy.xds.max_dt", "2501", "global");

        assertEquals("expy", term.getModule());
        assertEquals("xds", term.getThread());
        assertEquals("max_dt", term.getKey());
        assertEquals("2501", term.getValue());
    }

    @Test
    void appliesNewConfigEntityWithKeyAndValueWithoutPersisting() {
        ModuleEntity module = new ModuleEntity("expy");
        ThreadEntity thread = new ThreadEntity("xds", module, "expedition");
        ConfigEntry term = new ConfigEntry("expy", "xds", "max_dt", "2500");

        thread.applyUnsavedConfigUpdate(term);

        org.enoch.snark.db.entity.ConfigEntity created = thread.configs.getFirst();
        assertEquals("max_dt", created.key);
        assertEquals("2500", created.value);
        assertEquals(thread, created.thread);
    }

    @Test
    void listThreadDefinitionsIncludesInactiveThreadsAndStoredValues() {
        ModuleRepository moduleRepository = mock(ModuleRepository.class);
        ThreadRepository threadRepository = mock(ThreadRepository.class);
        ConfigRepository configRepository = mock(ConfigRepository.class);
        ModuleEntity module = new ModuleEntity("global");
        ThreadEntity thread = new ThreadEntity("update", module, "update");
        thread.time = "off";
        thread.configs.add(new org.enoch.snark.db.entity.ConfigEntity(thread, "refresh", "30M"));

        when(threadRepository.findAllWithModule()).thenReturn(List.of(thread));

        List<ThreadDefinition> definitions = new ConfigService(
                moduleRepository, threadRepository, configRepository).listThreadDefinitions();

        assertEquals(1, definitions.size());
        ThreadDefinition definition = definitions.getFirst();
        assertEquals("update", definition.getName());
        assertEquals("global", definition.getModule());
        assertEquals("off", definition.getConfigs().stream()
                .filter(config -> "time".equals(config.getKey()))
                .findFirst().orElseThrow().getValue());
        assertEquals("30M", definition.getConfigs().stream()
                .filter(config -> "refresh".equals(config.getKey()))
                .findFirst().orElseThrow().getValue());
    }

    @Test
    void updateThreadsUpdatesControlConfigAndRespondsWithoutFailure() {
        ModuleRepository moduleRepository = mock(ModuleRepository.class);
        ThreadRepository threadRepository = mock(ThreadRepository.class);
        ConfigRepository configRepository = mock(ConfigRepository.class);
        ModuleEntity module = new ModuleEntity("global");
        ThreadEntity thread = new ThreadEntity("update", module, "update");

        when(threadRepository.find("global", "update")).thenReturn(Optional.of(thread));

        ConfigEntry term = new ConfigEntry();
        term.setModule("global");
        term.setThread("update");
        term.setKey("time");
        term.setValue("12M");

        new ConfigService(moduleRepository, threadRepository, configRepository)
                .updateThreads(List.of(term));

        assertEquals("12M", thread.time);
        verify(threadRepository).save(thread);
        verify(configRepository, never()).saveAll(anyList());
    }

    @Test
    void updateThreadsPersistsOnlyCreatedCustomConfig() {
        ModuleRepository moduleRepository = mock(ModuleRepository.class);
        ThreadRepository threadRepository = mock(ThreadRepository.class);
        ConfigRepository configRepository = mock(ConfigRepository.class);
        ModuleEntity module = new ModuleEntity("expy");
        ThreadEntity thread = new ThreadEntity("xds", module, "expedition");

        when(threadRepository.find("expy", "xds")).thenReturn(Optional.of(thread));
        when(configRepository.save(any(org.enoch.snark.db.entity.ConfigEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConfigEntry term = new ConfigEntry("expy", "xds", "max_dt", "2501");

        new ConfigService(moduleRepository, threadRepository, configRepository)
                .updateThreads(List.of(term));

        ArgumentCaptor<org.enoch.snark.db.entity.ConfigEntity> captor =
                ArgumentCaptor.forClass(org.enoch.snark.db.entity.ConfigEntity.class);
        verify(configRepository).save(captor.capture());
        assertEquals("max_dt", captor.getValue().key);
        assertEquals("2501", captor.getValue().value);
    }

    @Test
    void updateThreadsUpdatesExistingCustomConfigAndPersistsIt() {
        ModuleRepository moduleRepository = mock(ModuleRepository.class);
        ThreadRepository threadRepository = mock(ThreadRepository.class);
        ConfigRepository configRepository = mock(ConfigRepository.class);
        ModuleEntity module = new ModuleEntity("expy");
        ThreadEntity thread = new ThreadEntity("xds", module, "expedition");
        org.enoch.snark.db.entity.ConfigEntity existing =
                new org.enoch.snark.db.entity.ConfigEntity(thread, "max_dt", "2500");

        when(threadRepository.find("expy", "xds")).thenReturn(Optional.of(thread));
        when(configRepository.findByThreadAndKey(thread, "max_dt")).thenReturn(Optional.of(existing));
        when(configRepository.save(existing)).thenReturn(existing);

        new ConfigService(moduleRepository, threadRepository, configRepository)
                .updateThreads(List.of(new ConfigEntry("expy", "xds", "max_dt", "2502")));

        assertEquals("2502", existing.value);
        verify(configRepository).save(existing);
    }

    @Test
    void updateThreadsUpdatesThreadCollectionExistingConfigWhenRepositoryLookupIsEmpty() {
        ModuleRepository moduleRepository = mock(ModuleRepository.class);
        ThreadRepository threadRepository = mock(ThreadRepository.class);
        ConfigRepository configRepository = mock(ConfigRepository.class);
        ModuleEntity module = new ModuleEntity("expy");
        ThreadEntity thread = new ThreadEntity("nowe", module, "expedition");
        org.enoch.snark.db.entity.ConfigEntity existing =
                new org.enoch.snark.db.entity.ConfigEntity(thread, "max_dt", "2500");

        when(threadRepository.find("expy", "nowe")).thenReturn(Optional.of(thread));
        when(configRepository.findByThreadAndKey(thread, "max_dt")).thenReturn(Optional.empty());
        when(configRepository.save(existing)).thenReturn(existing);

        new ConfigService(moduleRepository, threadRepository, configRepository)
                .updateThreads(List.of(new ConfigEntry("expy", "nowe", "max_dt", "2502")));

        assertEquals("2502", existing.value);
        verify(configRepository).save(existing);
        verify(configRepository, never()).save(argThat(config -> config != existing));
    }

    @Test
    void updateThreadsCreatesNamedThreadWithSelectedTypeAndDefaultTimeOff() {
        ModuleRepository moduleRepository = mock(ModuleRepository.class);
        ThreadRepository threadRepository = mock(ThreadRepository.class);
        ConfigRepository configRepository = mock(ConfigRepository.class);
        ModuleEntity module = new ModuleEntity("global");

        when(moduleRepository.findByName("global")).thenReturn(Optional.of(module));
        when(threadRepository.find("global", "update_main")).thenReturn(Optional.empty());
        when(threadRepository.save(any(ThreadEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConfigEntry term = new ConfigEntry();
        term.setModule("global");
        term.setThread("update_main");
        term.setKey("type");
        term.setValue("update");

        new ConfigService(moduleRepository, threadRepository, configRepository)
                .updateThreads(List.of(term));

        ArgumentCaptor<ThreadEntity> threadCaptor = ArgumentCaptor.forClass(ThreadEntity.class);
        verify(threadRepository, atLeastOnce()).save(threadCaptor.capture());
        assertEquals("update_main", threadCaptor.getAllValues().getFirst().name);
        assertEquals("update", threadCaptor.getAllValues().getFirst().type);
        assertEquals("off", threadCaptor.getAllValues().getFirst().time);
    }
}
