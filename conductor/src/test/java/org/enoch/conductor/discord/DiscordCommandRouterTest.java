package org.enoch.conductor.discord;

import org.enoch.conductor.instance.InstanceProfile;
import org.enoch.conductor.instance.ProfileRepository;
import org.enoch.conductor.process.ProcessService;
import org.enoch.common.status.NavigatorStatus;
import org.enoch.common.status.ServerStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DiscordCommandRouterTest {

    @Test
    void shouldReplyPongToPing() throws Exception {
        DiscordCommandRouter router = new DiscordCommandRouter(
                mock(ProfileRepository.class), mock(ProcessService.class));

        assertEquals("pong", router.handle("ping").render());
    }

    @Test
    void shouldSetActiveInstanceUsingShortCommandAndReportIt() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProcessService processService = mock(ProcessService.class);
        InstanceProfile profile = new InstanceProfile();
        profile.setId("server#user");
        profile.setDiscordChannelId("12345");
        when(profileRepository.findById("server#user")).thenReturn(Optional.of(profile));

        DiscordCommandRouter router = new DiscordCommandRouter(profileRepository, processService);
        DiscordCommandResult result = router.handle("set server#user");

        assertEquals("Active instance set to server#user", result.render());
    }

    @Test
    void shouldStartContextInstanceWithoutAnInstanceArgument() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProcessService processService = mock(ProcessService.class);
        InstanceProfile profile = profile("server#context", null);
        when(profileRepository.findById("server#context")).thenReturn(Optional.of(profile));
        DiscordCommandRouter router = new DiscordCommandRouter(profileRepository, processService);

        router.handle("set server#context");
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

        router.handle("set server#context");
        DiscordCommandResult result = router.handle("stop");

        assertTrue(result.render().contains("Stop request accepted for instance server#context"));
        verify(processService).stop("server#context");
    }

    @Test
    void shouldRestartContextInstanceWithoutAnInstanceArgument() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProcessService processService = mock(ProcessService.class);
        InstanceProfile profile = profile("server#context", null);
        when(profileRepository.findById("server#context")).thenReturn(Optional.of(profile));
        DiscordCommandRouter router = new DiscordCommandRouter(profileRepository, processService);

        router.handle("set server#context");
        DiscordCommandResult result = router.handle("restart");

        assertTrue(result.render().contains("Restart request accepted for instance server#context"));
        verify(processService).restart(profile);
    }

    @Test
    void shouldReportStatusOfContextInstanceWithoutAnInstanceArgument() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProcessService processService = mock(ProcessService.class);
        InstanceProfile profile = profile("server#context", "987654321098765432");
        when(profileRepository.findById("server#context")).thenReturn(Optional.of(profile));
        when(processService.getState("server#context")).thenReturn("running");
        NavigatorStatus navigatorStatus = new NavigatorStatus(
                3, 20, 1, 3, Map.of("expedition", 1L), LocalDateTime.of(2026, 11, 1, 21, 44, 5));
        when(processService.getLastServerStatus("server#context"))
                .thenReturn(new ServerStatus(navigatorStatus, List.of(), "", "", null));
        DiscordCommandRouter router = new DiscordCommandRouter(profileRepository, processService);

        router.handle("set server#context");
        DiscordCommandResult result = router.handle("status");

        assertEquals("server#context is running. State 3 / 20 1 / 3 e:1 as of 21:44:05.", result.render());
        assertFalse(result.render().contains("Discord channel"));
    }

    @Test
    void shouldListActiveThreadsForContextInstance() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProcessService processService = mock(ProcessService.class);
        InstanceProfile profile = profile("server#context", null);
        when(profileRepository.findById("server#context")).thenReturn(Optional.of(profile));
        when(processService.getLastServerStatus("server#context"))
                .thenReturn(new ServerStatus(null, List.of("NavigatorThread", "FleetThread"), "", "", null));
        DiscordCommandRouter router = new DiscordCommandRouter(profileRepository, processService);
        router.handle("set server#context");

        assertEquals("NavigatorThread" + System.lineSeparator() + "FleetThread",
                router.handle("status thread").render());
    }

    @Test
    void shouldShowCommandDequeForContextInstance() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProcessService processService = mock(ProcessService.class);
        InstanceProfile profile = profile("server#context", null);
        when(profileRepository.findById("server#context")).thenReturn(Optional.of(profile));
        when(processService.getLastServerStatus("server#context"))
                .thenReturn(new ServerStatus(null, List.of(), "command A -> command B", "", null));
        DiscordCommandRouter router = new DiscordCommandRouter(profileRepository, processService);
        router.handle("set server#context");

        assertEquals("command A -> command B", router.handle("status deque").render());
    }

    @Test
    void shouldExplainCommandsAndNestedStatusThreadAction() throws Exception {
        DiscordCommandRouter router = new DiscordCommandRouter(
                mock(ProfileRepository.class), mock(ProcessService.class));

        assertTrue(router.handle("list help").render().contains("Lists all configured instances"));
        assertTrue(router.handle("status help").render().contains("Shows the selected instance status"));
        assertTrue(router.handle("status thread help").render().contains("Lists the active threads"));
        assertTrue(router.handle("status deque help").render().contains("Shows the selected instance command deque"));
        assertTrue(router.handle("set help").render().contains("Selects the active instance"));
        assertTrue(router.handle("set server#context help").render().contains("Selects the active instance"));
        assertTrue(router.handle("ping help").render().contains("Replies with pong"));
        assertTrue(router.handle("status unknown").render().startsWith("Unknown action: status unknown."));
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
        DiscordCommandResult result = router.handle("list");

        assertTrue(result.render().contains("server#running"));
        assertTrue(result.render().contains("server#stopped"));
        assertTrue(result.render().contains("server#no-channel"));
        assertTrue(result.render().contains("state=running"));
        assertTrue(result.render().contains("state=stopped"));
        assertTrue(result.render().contains("discord=none"));
    }

    @Test
    void shouldUseContextAsTheContextCommand() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProcessService processService = mock(ProcessService.class);
        InstanceProfile profile = profile("server#context", null);
        when(profileRepository.findById("server#context")).thenReturn(Optional.of(profile));
        when(processService.getState("server#context")).thenReturn("running");
        DiscordCommandRouter router = new DiscordCommandRouter(profileRepository, processService);
        router.handle("set server#context");

        DiscordCommandResult result = router.handle("context");

        assertTrue(result.render().contains("Active instance: server#context"));
    }

    @Test
    void shouldNotAcceptReplacedLongCommandNames() throws Exception {
        DiscordCommandRouter router = new DiscordCommandRouter(
                mock(ProfileRepository.class), mock(ProcessService.class));

        assertTrue(router.handle("list instances").render().contains("Instance IDs are not accepted"));
        assertTrue(router.handle("get context").render().startsWith("Unknown action: get context."));
        assertTrue(router.handle("show context").render().startsWith("Unknown action: show context."));
        assertTrue(router.handle("set active instance server#context").render().contains("Use the shortened set command."));
    }

    @Test
    void shouldRenderOnlySummaryAndOmitDetails() {
        DiscordCommandResult result = DiscordCommandResult.failure("test", "Summary text", "Internal details");

        assertEquals("Summary text", result.render());
    }

    @Test
    void shouldIncludeUnknownActionAndHelpHint() throws Exception {
        DiscordCommandRouter router = new DiscordCommandRouter(
                mock(ProfileRepository.class), mock(ProcessService.class));

        assertEquals(
                "Unknown action: frobnicate. Type help to see the available commands.",
                router.handle("frobnicate").render());
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

        DiscordCommandResult result = router.handle("create instance");

        assertTrue(result.render().contains("not available"));
    }
}
