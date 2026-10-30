package org.enoch.conductor.process;

import org.enoch.conductor.instance.InstanceProfile;
import org.enoch.conductor.instance.InstanceRuntime;
import org.enoch.conductor.instance.ProfileRepository;
import org.enoch.conductor.ws.WorkerSocketHandler;
import org.enoch.common.status.ServerStatus;
import org.enoch.common.status.StatusDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class ProcessService {

    private final Map<String, InstanceRuntime> runtimes = new ConcurrentHashMap<>();
    private final Map<String, InstanceProfile> managedProfiles = new ConcurrentHashMap<>();
    private final Set<String> restartingInstances = ConcurrentHashMap.newKeySet();
    private final String instancesDirPath;
    private final long workerDisconnectTimeoutMillis;
    private final ProfileRepository profileRepository;
    private final WorkerSocketHandler workerSocketHandler;
    private final WorkerProcessLauncher workerProcessLauncher;
    private final InstanceLogService instanceLogService;

    public ProcessService(@Value("${conductor.instances-dir:instances}") String instancesDirPath,
                          @Value("${conductor.worker-disconnect-timeout:30000}") long workerDisconnectTimeoutMillis,
                          ProfileRepository profileRepository,
                          WorkerSocketHandler workerSocketHandler,
                          WorkerProcessLauncher workerProcessLauncher,
                          InstanceLogService instanceLogService) {
        this.instancesDirPath = instancesDirPath;
        this.workerDisconnectTimeoutMillis = workerDisconnectTimeoutMillis;
        this.profileRepository = profileRepository;
        this.workerSocketHandler = workerSocketHandler;
        this.workerProcessLauncher = workerProcessLauncher;
        this.instanceLogService = instanceLogService;
        this.workerSocketHandler.setRestartRequestHandler(this::requestRestart);
    }

    @PostConstruct
    public void autoStartInstances() throws Exception {
        List<InstanceProfile> profiles = profileRepository.loadAll();

        for (InstanceProfile profile : profiles) {
            if (profile.isAutoStart()) {
                try {
                    System.out.println("Auto-starting instance: " + profile.getId());
                    start(profile);
                } catch (Exception e) {
                    System.err.println("Failed to auto-start instance " + profile.getId());
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Auto-starting instances completed. Currently running instances: " + runtimes.keySet());
    }

    /**
     * Starts an instance based on its profile.
     */
    public synchronized void start(InstanceProfile profile) throws Exception {
        managedProfiles.put(profile.getId(), profile);
        InstanceRuntime currentRuntime = runtimes.get(profile.getId());
        if (currentRuntime != null && currentRuntime.getProcess().isAlive()) {
            System.out.println("Instance with id " + profile.getId() + " is already running");
            return;
        }
        if (currentRuntime != null) {
            runtimes.remove(profile.getId(), currentRuntime);
        }

        Path instanceDir = Path.of(instancesDirPath, profile.getId());
        Files.createDirectories(instanceDir);
        Path propertiesPath = profileRepository.resolvePropertiesPath(profile);
        instanceLogService.prepareLogs(instanceDir);
        long startedAt = System.currentTimeMillis();
        Process process = workerProcessLauncher.launch(profile, instanceDir, propertiesPath);
        try {
            instanceLogService.capture(profile.getId(), process, instanceDir);
        } catch (IOException e) {
            process.destroyForcibly();
            try {
                process.waitFor(5, TimeUnit.SECONDS);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                e.addSuppressed(interruptedException);
            }
            throw e;
        }

        InstanceRuntime runtime = new InstanceRuntime();
        runtime.setProcess(process);
        runtime.setPid(process.pid());
        runtime.setOnline(true);
        runtime.setStartedAt(startedAt);
        runtimes.put(profile.getId(), runtime);
        System.out.println("Started instance with id " + profile.getId() + " and pid " + process.pid());
    }

    /**
     * Stops a running instance.
     */
    public synchronized void stop(String id) {
        managedProfiles.remove(id);
        restartingInstances.remove(id);
        stopRuntime(id, runtimes.get(id));
    }

    private void stopRuntime(String id, InstanceRuntime runtime) {

        if (runtime == null) {
            System.err.println("Instance with id " + id + " is not running");
            return;
        }

        System.err.println("Stopping instance with id " + id + " and pid " + runtime.getPid());
        Process process = runtime.getProcess();
        process.destroy();
        try {
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Process did not terminate after forced shutdown: " + id);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while stopping instance " + id, e);
        }
        runtime.setOnline(false);

        runtimes.remove(id, runtime);
    }

    /**
     * Restarts an instance.
     */
    public synchronized void restart(InstanceProfile profile) throws Exception {
        managedProfiles.put(profile.getId(), profile);
        restartingInstances.add(profile.getId());
        try {
            stopRuntime(profile.getId(), runtimes.get(profile.getId()));
            Thread.sleep(1000);
            start(profile);
        } finally {
            restartingInstances.remove(profile.getId());
        }
    }

    @Scheduled(fixedDelayString = "${conductor.instances-monitor-interval:5000}")
    public synchronized void recoverUnexpectedlyStoppedInstances() {
        for (Map.Entry<String, InstanceProfile> entry : managedProfiles.entrySet()) {
            String id = entry.getKey();
            InstanceProfile profile = entry.getValue();
            InstanceRuntime runtime = runtimes.get(id);
            try {
                if (runtime != null && runtime.isRestartRequested()) {
                    System.err.println("Worker requested restart; restarting instance " + id);
                    restart(profile);
                    continue;
                }
                if (runtime == null || !runtime.getProcess().isAlive()) {
                    if (runtime != null) {
                        runtime.setOnline(false);
                        runtimes.remove(id, runtime);
                        System.err.println("Worker process exited unexpectedly; restarting instance " + id);
                    }
                    start(profile);
                    continue;
                }

                if (workerSocketHandler.isRegistered(id)) {
                    runtime.setWorkerRegistered(true);
                    runtime.setDisconnectedSince(0);
                } else if (hasRegisteredCurrentProcess(runtime, id)) {
                    runtime.setWorkerRegistered(true);
                    long now = System.currentTimeMillis();
                    if (runtime.getDisconnectedSince() == 0) {
                        runtime.setDisconnectedSince(now);
                    } else if (now - runtime.getDisconnectedSince() >= workerDisconnectTimeoutMillis) {
                        System.err.println("Worker remained disconnected; restarting instance " + id);
                        stopRuntime(id, runtime);
                        start(profile);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to recover instance " + id + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void requestRestart(String id, String reason) {
        InstanceRuntime runtime = runtimes.get(id);
        if (runtime == null || !runtime.getProcess().isAlive()) {
            System.err.println("Ignoring restart request from an unmanaged or stopped worker " + id);
            return;
        }
        runtime.setRestartRequested(true);
        restartingInstances.add(id);
        System.err.println("Worker restart requested for " + id + ": " + reason);
    }

    /**
     * Checks if an instance is running.
     */
    public boolean isRunning(String id) {
        InstanceRuntime runtime = runtimes.get(id);
        return runtime != null && runtime.getProcess().isAlive();
    }

    public String getState(String id) {
        if (restartingInstances.contains(id)) {
            return "restarting";
        }
        InstanceRuntime runtime = runtimes.get(id);
        if (runtime == null || !runtime.getProcess().isAlive()) {
            return "stopped";
        }

        if (runtime.isRestartRequested()) {
            return "restarting";
        }

        String lastStatusMessage = getLastStatusMessage(id);
        if (lastStatusMessage != null && lastStatusMessage.startsWith("Waiting for next attempt at ")) {
            return "logged_out";
        }

        if (workerSocketHandler.isRegistered(id)) {
            runtime.setWorkerRegistered(true);
            runtime.setDisconnectedSince(0);
            return getLastStatusUpdatedAt(id) >= runtime.getStartedAt() ? "running" : "starting";
        }

        return hasRegisteredCurrentProcess(runtime, id) ? "disconnected" : "starting";
    }

    private boolean hasRegisteredCurrentProcess(InstanceRuntime runtime, String id) {
        return runtime.hasWorkerRegistered()
                || workerSocketHandler.getLastRegisteredAt(id) >= runtime.getStartedAt();
    }

    /**
     * Gets the runtime information for an instance.
     */
    public InstanceRuntime getRuntime(String id) {
        return runtimes.get(id);
    }

    public boolean requestStatus(String id) throws Exception {
        return workerSocketHandler.requestStatus(id);
    }

    public StatusDetails requestStatusDetails(String id, int offset, int limit) throws Exception {
        return workerSocketHandler.requestStatusDetails(id, offset, limit);
    }

    public String getLastStatusMessage(String id) {
        return workerSocketHandler.getLastStatusMessage(id);
    }

    public ServerStatus getLastServerStatus(String id) {
        return workerSocketHandler.getLastServerStatus(id);
    }

    public long getLastStatusUpdatedAt(String id) {
        return workerSocketHandler.getLastStatusUpdatedAt(id);
    }

    public boolean isWorkerConnected(String id) {
        return workerSocketHandler.isRegistered(id);
    }

    /**
     * Gets all running runtimes.
     */
    public Map<String, InstanceRuntime> getAllRuntimes() {
        return new ConcurrentHashMap<>(runtimes);
    }
}