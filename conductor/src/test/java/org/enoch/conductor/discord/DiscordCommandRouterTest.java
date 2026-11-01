package org.enoch.conductor.discord;

import org.enoch.conductor.instance.InstanceProfile;
import org.enoch.conductor.instance.ProfileRepository;
import org.enoch.conductor.process.ProcessService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    void shouldStartContextInstanceWithoutAnInstanceArgument() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProcessService processService = mock(ProcessService.class);
        InstanceProfile profile = profile("server#context", null);
        when(profileRepository.findById("server#context")).thenReturn(Optional.of(profile));
        DiscordCommandRouter router = new DiscordCommandRouter(profileRepository, processService);

        router.handle("set active instance server#context");
        DiscordCommandResult result = router.handle("start");

        assertTrue(result.render().contains("Start request accepted for instance server#context"));
        verify(processService).start(profile);
    }

    @Test
    void shouldStopContextInstanceWithoutAnInstanceArgument() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProcessService processService = mock(ProcessService.class);
        InstanceProfile profile = profile("server#context", null);
        when(profileRepository.findById("server#context")).thenReturn(Optional.of(profile));
        DiscordCommandRouter router = new DiscordCommandRouter(profileRepository, processService);

        router.handle("set active instance server#context");
        DiscordCommandResult result = router.handle("stop");

        assertTrue(result.render().contains("Stop request accepted for instance server#context"));
        verify(processService).stop("server#context");
    }

    @Test
    void shouldReportStatusOfContextInstanceWithoutAnInstanceArgument() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProcessService processService = mock(ProcessService.class);
        InstanceProfile profile = profile("server#context", null);
        when(profileRepository.findById("server#context")).thenReturn(Optional.of(profile));
        when(processService.getState("server#context")).thenReturn("running");
        DiscordCommandRouter router = new DiscordCommandRouter(profileRepository, processService);

        router.handle("set active instance server#context");
        DiscordCommandResult result = router.handle("status");

        assertTrue(result.render().contains("Instance server#context is in state running"));
    }

    @Test
    void shouldRejectInstanceArgumentOnContextCommands() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProcessService processService = mock(ProcessService.class);
        DiscordCommandRouter router = new DiscordCommandRouter(profileRepository, processService);

        DiscordCommandResult result = router.handle("start server#other");

        assertTrue(result.render().contains("Instance IDs are not accepted"));
        verify(processService, never()).start(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldListEveryInstanceRegardlessOfStateOrDiscordChannel() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProcessService processService = mock(ProcessService.class);
        InstanceProfile runningWithChannel = profile("server#running", "123456789012345678");
        InstanceProfile stoppedWithoutChannel = profile("server#stopped", null);
        InstanceProfile runningWithoutChannel = profile("server#no-channel", "");
        when(profileRepository.loadAll()).thenReturn(
                List.of(runningWithChannel, stoppedWithoutChannel, runningWithoutChannel));
        when(processService.getState("server#running")).thenReturn("running");
        when(processService.getState("server#stopped")).thenReturn("stopped");
        when(processService.getState("server#no-channel")).thenReturn("running");

        DiscordCommandRouter router = new DiscordCommandRouter(profileRepository, processService);
        DiscordCommandResult result = router.handle("list instances");

        assertTrue(result.render().contains("server#running"));
        assertTrue(result.render().contains("server#stopped"));
        assertTrue(result.render().contains("server#no-channel"));
        assertTrue(result.render().contains("state=running"));
        assertTrue(result.render().contains("state=stopped"));
        assertTrue(result.render().contains("discord=none"));
    }

    private InstanceProfile profile(String id, String discordChannelId) {
        InstanceProfile profile = new InstanceProfile();
        profile.setId(id);
        profile.setDiscordChannelId(discordChannelId);
        return profile;
    }

    @Test
    void shouldReportUnsupportedCommandsAsInactive() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProcessService processService = mock(ProcessService.class);
        DiscordCommandRouter router = new DiscordCommandRouter(profileRepository, processService);

        DiscordCommandResult result = router.handle("restart");

        assertTrue(result.render().contains("inactive"));
        assertTrue(result.render().contains("not available"));
    }
}
