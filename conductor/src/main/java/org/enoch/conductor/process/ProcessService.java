package org.enoch.conductor.process;

import org.enoch.conductor.instance.InstanceProfile;
import org.enoch.conductor.instance.InstanceRuntime;
import org.enoch.conductor.instance.ProfileRepository;
import org.enoch.conductor.ws.WorkerSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
public class ProcessService {

    private final Map<String, InstanceRuntime> runtimes = new ConcurrentHashMap<>();
    private final String instancesDirPath;
    private final ProfileRepository profileRepository;
    private final WorkerSocketHandler workerSocketHandler;
    private final String configuredWorkerJar;

    public ProcessService(@Value("${conductor.instances-dir:instances}") String instancesDirPath,
                          @Value("${conductor.worker:}") String configuredWorkerJar,
                          ProfileRepository profileRepository,
                          WorkerSocketHandler workerSocketHandler) {
        this.instancesDirPath = instancesDirPath;
        this.configuredWorkerJar = configuredWorkerJar;
        this.profileRepository = profileRepository;
        this.workerSocketHandler = workerSocketHandler;
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
    public void start(InstanceProfile profile) throws Exception {
        if (runtimes.containsKey(profile.getId())) {
            System.out.println("Instance with id " + profile.getId() + " is already running");
            return;
        }

        Path instanceDir = Path.of(instancesDirPath, profile.getId());
        Files.createDirectories(instanceDir);

        String workerJarPath = resolveWorkerJar();
        System.out.println("Using worker jar: " + workerJarPath);

        List<String> command = new ArrayList<>();
        command.add("java");

        if (profile.getDatabaseName() != null && !profile.getDatabaseName().isBlank()) {
            command.add("-Ddatabase-name=" + profile.getDatabaseName().trim());
        }

        Path propertiesPath = profileRepository.resolvePropertiesPath(profile);
        command.add("-Dproperties=" + propertiesPath);

        command.add("-jar");
        command.add(workerJarPath);
        command.add("--instanceId=" + profile.getId());
        command.add("--instanceDir=" + instanceDir);
        command.add("--managerUrl=ws://localhost:8080/ws");

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();

        Process process = pb.start();

        InstanceRuntime runtime = new InstanceRuntime();
        runtime.setProcess(process);
        runtime.setPid(process.pid());
        runtime.setOnline(true);

        runtimes.put(profile.getId(), runtime);
        System.out.println("Started instance with id " + profile.getId() + " and pid " + process.pid());
    }

    private String resolveWorkerJar() throws IOException {
        String explicitPath = configuredWorkerJar == null ? "" : configuredWorkerJar.trim();
        if (!explicitPath.isBlank()) {
            Path candidate = Path.of(explicitPath);
            if (Files.isRegularFile(candidate)) {
                return candidate.toAbsolutePath().normalize().toString();
            }
            throw new IOException("Configured worker jar does not exist: " + candidate.toAbsolutePath());
        }

        Path currentDir = Path.of("").toAbsolutePath().normalize();
        List<Path> candidates = new ArrayList<>();

        try (Stream<Path> stream = Files.list(currentDir)) {
            stream.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                    .filter(path -> !path.getFileName().toString().toLowerCase(Locale.ROOT).startsWith("conductor"))
                    .forEach(candidates::add);
        }

        if (!candidates.isEmpty()) {
            return candidates.stream()
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .findFirst()
                    .orElseThrow()
                    .toAbsolutePath()
                    .normalize()
                    .toString();
        }

        Path snarkTarget = Path.of("snark", "target").toAbsolutePath().normalize();
        if (Files.isDirectory(snarkTarget)) {
            try (Stream<Path> stream = Files.list(snarkTarget)) {
                List<Path> snarkJars = stream
                        .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                        .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                        .toList();

                if (!snarkJars.isEmpty()) {
                    return snarkJars.getFirst().toAbsolutePath().normalize().toString();
                }
            }
        }

        throw new IOException("No worker jar found. Provide --worker=/path/to/worker.jar or place a non-conductor jar in the working directory or in snark/target.");
    }

    /**
     * Stops a running instance.
     */
    public void stop(String id) {
        InstanceRuntime runtime = runtimes.get(id);

        if (runtime == null) {
            System.err.println("Instance with id " + id + " is not running");
            return;
        }

        System.err.println("Stopping instance with id " + id + " and pid " + runtime.getPid());
        runtime.getProcess().destroy();
        try {
            if (!runtime.getProcess().waitFor(5, TimeUnit.SECONDS)) {
                runtime.getProcess().destroyForcibly();
                runtime.getProcess().waitFor(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while stopping instance " + id, e);
        }
        runtime.setOnline(false);

        runtimes.remove(id);
    }

    /**
     * Restarts an instance.
     */
    public void restart(InstanceProfile profile) throws Exception {
        stop(profile.getId());
        Thread.sleep(1000);
        start(profile);
    }

    /**
     * Checks if an instance is running.
     */
    public boolean isRunning(String id) {
        InstanceRuntime runtime = runtimes.get(id);
        return runtime != null && runtime.getProcess().isAlive();
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

    public String getLastStatusMessage(String id) {
        return workerSocketHandler.getLastStatusMessage(id);
    }

    public long getLastStatusUpdatedAt(String id) {
        return workerSocketHandler.getLastStatusUpdatedAt(id);
    }

    /**
     * Gets all running runtimes.
     */
    public Map<String, InstanceRuntime> getAllRuntimes() {
        return new ConcurrentHashMap<>(runtimes);
    }
}