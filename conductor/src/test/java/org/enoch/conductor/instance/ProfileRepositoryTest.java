package org.enoch.conductor.instance;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.enoch.conductor.settings.ConductorSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfileRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldPersistDiscordChannelIdInInstanceConfig() throws Exception {
        ConductorSettings settings = mock(ConductorSettings.class);
        when(settings.getInstancesDir()).thenReturn(tempDir.toString());
        ProfileRepository repository = new ProfileRepository(settings);

        InstanceProfile profile = new InstanceProfile();
        profile.setId("instance-1");
        profile.setDiscordChannelId("123456789012345678");
        repository.save(profile);

        InstanceProfile savedProfile = new ObjectMapper().readValue(
                tempDir.resolve("instance-1").resolve("config.json").toFile(),
                InstanceProfile.class
        );
        assertEquals("123456789012345678", savedProfile.getDiscordChannelId());
    }
}
