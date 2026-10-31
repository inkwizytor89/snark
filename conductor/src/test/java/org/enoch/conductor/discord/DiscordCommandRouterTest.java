package org.enoch.conductor.discord;

import org.enoch.conductor.instance.InstanceProfile;
import org.enoch.conductor.instance.ProfileRepository;
import org.enoch.conductor.process.ProcessService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DiscordCommandRouterTest {

    @Test
    void shouldSetActiveInstanceAndReportIt() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProcessService processService = mock(ProcessService.class);
        InstanceProfile profile = new InstanceProfile();
        profile.setId("server#user");
        profile.setDiscordChannelId("12345");
        when(profileRepository.findById("server#user")).thenReturn(Optional.of(profile));

        DiscordCommandRouter router = new DiscordCommandRouter(profileRepository, processService);
        DiscordCommandResult result = router.handle("set active instance server#user");

        assertTrue(result.render().contains("Command: set active instance"));
        assertTrue(result.render().contains("Active instance set to server#user"));
    }

    @Test
    void shouldListConfiguredInstancesWithState() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProcessService processService = mock(ProcessService.class);
        InstanceProfile profile = new InstanceProfile();
        profile.setId("server#user");
        profile.setDiscordChannelId("12345");
        when(profileRepository.loadAll()).thenReturn(List.of(profile));
        when(processService.getState("server#user")).thenReturn("running");

        DiscordCommandRouter router = new DiscordCommandRouter(profileRepository, processService);
        DiscordCommandResult result = router.handle("list instances");

        assertTrue(result.render().contains("server#user"));
        assertTrue(result.render().contains("state=running"));
    }
}
