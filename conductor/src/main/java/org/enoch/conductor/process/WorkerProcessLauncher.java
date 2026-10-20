package org.enoch.conductor.process;

import org.enoch.conductor.instance.InstanceProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
public class WorkerProcessLauncher {

    private final String configuredWorkerJar;

    public WorkerProcessLauncher(@Value("${conductor.worker:}") String configuredWorkerJar) {
        this.configuredWorkerJar = configuredWorkerJar;
    }

    public Process launch(InstanceProfile profile, Path instanceDir, Path propertiesPath) throws IOException {
        String workerJarPath = resolveWorkerJar();
        System.out.println("Using worker jar: " + workerJarPath);

        List<String> command = new ArrayList<>();
        command.add("java");

        if (profile.getDatabaseName() != null && !profile.getDatabaseName().isBlank()) {
            command.add("-Ddatabase-name=" + profile.getDatabaseName().trim());
        }

        command.add("-Dproperties=" + propertiesPath);
        command.add("-jar");
        command.add(workerJarPath);
        command.add("--instanceId=" + profile.getId());
        command.add("--instanceDir=" + instanceDir);
        command.add("--managerUrl=ws://localhost:8080/ws");

        return new ProcessBuilder(command).start();
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
            stream.filter(path -> Files.isRegularFile(path)
                            && path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
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
                        .filter(path -> Files.isRegularFile(path)
                                && path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                        .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                        .toList();

                if (!snarkJars.isEmpty()) {
                    return snarkJars.getFirst().toAbsolutePath().normalize().toString();
                }
            }
        }

        throw new IOException("No worker jar found. Provide --worker=/path/to/worker.jar or place a non-conductor jar in the working directory or in snark/target.");
    }
}
