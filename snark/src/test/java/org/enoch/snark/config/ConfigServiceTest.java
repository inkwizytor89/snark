package org.enoch.snark.config;

import org.enoch.snark.db.entity.ModuleEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.enoch.snark.db.repository.ConfigRepository;
import org.enoch.snark.db.repository.ModuleRepository;
import org.enoch.snark.db.repository.ThreadRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        ConfigTerm term = new ConfigTerm();
        term.setModule("module");
        term.setThread("expedition_day");
        term.setKey("time");
        term.setValue("on");

        new ConfigService(moduleRepository, threadRepository, configRepository)
                .updateThreads(List.of(term));

        ArgumentCaptor<ThreadEntity> threadCaptor = ArgumentCaptor.forClass(ThreadEntity.class);
        verify(threadRepository).save(threadCaptor.capture());
        assertEquals("expedition", threadCaptor.getValue().type);
    }
}
