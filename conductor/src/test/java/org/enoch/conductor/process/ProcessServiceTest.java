package org.enoch.conductor.process;

import org.enoch.conductor.instance.InstanceProfile;
import org.enoch.conductor.instance.ProfileRepository;
import org.enoch.conductor.ws.WorkerSocketHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;

class ProcessServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldNotRestartConnectedWorkerWhileItIsWaitingInLobby() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        WorkerSocketHandler workerSocketHandler = mock(WorkerSocketHandler.class);
        WorkerProcessLauncher launcher = mock(WorkerProcessLauncher.class);
        InstanceLogService logService = mock(InstanceLogService.class);
        Process process = mock(Process.class);
        InstanceProfile profile = profile("worker#1");
        when(profileRepository.resolvePropertiesPath(profile)).thenReturn(temporaryDirectory.resolve("worker.properties"));
        when(launcher.launch(eq(profile), any(Path.class), any(Path.class))).thenReturn(process);
        when(process.isAlive()).thenReturn(true);
        when(process.pid()).thenReturn(42L);
        when(workerSocketHandler.isRegistered(profile.getId())).thenReturn(true);

        ProcessService service = new ProcessService(
                temporaryDirectory.toString(), 30_000L, profileRepository,
                workerSocketHandler, launcher, logService);
        service.start(profile);
        when(workerSocketHandler.getLastStatusUpdatedAt(profile.getId()))
                .thenReturn(service.getRuntime(profile.getId()).getStartedAt() + 1);

        service.recoverUnexpectedlyStoppedInstances();

        assertSame(process, service.getRuntime(profile.getId()).getProcess());
        assertEquals("running", service.getState(profile.getId()));
        verify(launcher, times(1)).launch(eq(profile), any(Path.class), any(Path.class));
    }

    @Test
    void shouldReportStartingUntilTheWorkerSendsAStatusUpdate() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        WorkerSocketHandler workerSocketHandler = mock(WorkerSocketHandler.class);
        WorkerProcessLauncher launcher = mock(WorkerProcessLauncher.class);
        InstanceLogService logService = mock(InstanceLogService.class);
        Process process = mock(Process.class);
        InstanceProfile profile = profile("worker#starting");
        when(profileRepository.resolvePropertiesPath(profile)).thenReturn(temporaryDirectory.resolve("worker.properties"));
        when(launcher.launch(eq(profile), any(Path.class), any(Path.class))).thenReturn(process);
        when(process.isAlive()).thenReturn(true);
        when(process.pid()).thenReturn(47L);
        when(workerSocketHandler.isRegistered(profile.getId())).thenReturn(true, true, false);

        ProcessService service = new ProcessService(
                temporaryDirectory.toString(), 30_000L, profileRepository,
                workerSocketHandler, launcher, logService);
        service.start(profile);
        long startedAt = service.getRuntime(profile.getId()).getStartedAt();
        when(workerSocketHandler.getLastStatusUpdatedAt(profile.getId()))
                .thenReturn(0L, startedAt + 1);

        assertEquals("starting", service.getState(profile.getId()));
        assertEquals("running", service.getState(profile.getId()));
        assertEquals("disconnected", service.getState(profile.getId()));
    }

    @Test
    void shouldRestartWorkerAfterItsProcessExits() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        WorkerSocketHandler workerSocketHandler = mock(WorkerSocketHandler.class);
        WorkerProcessLauncher launcher = mock(WorkerProcessLauncher.class);
        InstanceLogService logService = mock(InstanceLogService.class);
        Process exitedProcess = mock(Process.class);
        Process replacementProcess = mock(Process.class);
        InstanceProfile profile = profile("worker#2");
        when(profileRepository.resolvePropertiesPath(profile)).thenReturn(temporaryDirectory.resolve("worker.properties"));
        when(launcher.launch(eq(profile), any(Path.class), any(Path.class)))
                .thenReturn(exitedProcess, replacementProcess);
        when(exitedProcess.isAlive()).thenReturn(false);
        when(exitedProcess.pid()).thenReturn(43L);
        when(replacementProcess.isAlive()).thenReturn(true);
        when(replacementProcess.pid()).thenReturn(44L);

        ProcessService service = new ProcessService(
                temporaryDirectory.toString(), 30_000L, profileRepository,
                workerSocketHandler, launcher, logService);
        service.start(profile);
        service.recoverUnexpectedlyStoppedInstances();

        assertSame(replacementProcess, service.getRuntime(profile.getId()).getProcess());
        verify(launcher, times(2)).launch(eq(profile), any(Path.class), any(Path.class));
    }

    @Test
    void shouldForceAndRestartWorkerAfterARegisteredConnectionIsLost() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        WorkerSocketHandler workerSocketHandler = mock(WorkerSocketHandler.class);
        WorkerProcessLauncher launcher = mock(WorkerProcessLauncher.class);
        InstanceLogService logService = mock(InstanceLogService.class);
        Process disconnectedProcess = mock(Process.class);
        Process replacementProcess = mock(Process.class);
        InstanceProfile profile = profile("worker#3");
        when(profileRepository.resolvePropertiesPath(profile)).thenReturn(temporaryDirectory.resolve("worker.properties"));
        when(launcher.launch(eq(profile), any(Path.class), any(Path.class)))
                .thenReturn(disconnectedProcess, replacementProcess);
        when(disconnectedProcess.isAlive()).thenReturn(true);
        when(disconnectedProcess.pid()).thenReturn(45L);
        when(disconnectedProcess.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(false, true);
        when(replacementProcess.isAlive()).thenReturn(true);
        when(replacementProcess.pid()).thenReturn(46L);
        when(workerSocketHandler.isRegistered(profile.getId())).thenReturn(true, false, false);

        ProcessService service = new ProcessService(
                temporaryDirectory.toString(), 0, profileRepository,
                workerSocketHandler, launcher, logService);
        service.start(profile);
        service.recoverUnexpectedlyStoppedInstances();
        service.recoverUnexpectedlyStoppedInstances();
        service.recoverUnexpectedlyStoppedInstances();

        assertSame(replacementProcess, service.getRuntime(profile.getId()).getProcess());
        verify(disconnectedProcess).destroy();
        verify(disconnectedProcess).destroyForcibly();
        verify(launcher, times(2)).launch(eq(profile), any(Path.class), any(Path.class));
    }

    @Test
    void shouldForceAndRelaunchWorkerWhenItRequestsRestart() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        WorkerSocketHandler workerSocketHandler = mock(WorkerSocketHandler.class);
        WorkerProcessLauncher launcher = mock(WorkerProcessLauncher.class);
        InstanceLogService logService = mock(InstanceLogService.class);
        Process currentProcess = mock(Process.class);
        Process replacementProcess = mock(Process.class);
        InstanceProfile profile = profile("worker#restart");
        when(profileRepository.resolvePropertiesPath(profile)).thenReturn(temporaryDirectory.resolve("worker.properties"));
        when(launcher.launch(eq(profile), any(Path.class), any(Path.class)))
                .thenReturn(currentProcess, replacementProcess);
        when(currentProcess.isAlive()).thenReturn(true);
        when(currentProcess.pid()).thenReturn(49L);
        when(currentProcess.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(replacementProcess.isAlive()).thenReturn(true);
        when(replacementProcess.pid()).thenReturn(50L);

        AtomicReference<BiConsumer<String, String>> restartHandler = new AtomicReference<>();
        doAnswer(invocation -> {
            restartHandler.set(invocation.getArgument(0));
            return null;
        }).when(workerSocketHandler).setRestartRequestHandler(any());

        ProcessService service = new ProcessService(
                temporaryDirectory.toString(), 30_000L, profileRepository,
                workerSocketHandler, launcher, logService);
        service.start(profile);

        assertNotNull(restartHandler.get());
        restartHandler.get().accept(profile.getId(), "ChromeDriver window is unavailable");
        assertEquals("restarting", service.getState(profile.getId()));

        service.recoverUnexpectedlyStoppedInstances();

        assertSame(replacementProcess, service.getRuntime(profile.getId()).getProcess());
        verify(currentProcess).destroy();
        verify(launcher, times(2)).launch(eq(profile), any(Path.class), any(Path.class));
    }

    private InstanceProfile profile(String id) {
        InstanceProfile profile = new InstanceProfile();
        profile.setId(id);
        profile.setProperties("worker.properties");
        return profile;
    }
}
