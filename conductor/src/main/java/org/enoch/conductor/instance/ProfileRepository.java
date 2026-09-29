package org.enoch.conductor.instance;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProfileRepository {

    private final ObjectMapper mapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private final Path instancesDir;

    public ProfileRepository(@Value("${conductor.instances-dir:instances}") String instancesDirPath) {
        this.instancesDir = Path.of(instancesDirPath);
    }

    public List<InstanceProfile> loadAll() throws Exception {

        List<InstanceProfile> profiles = new ArrayList<>();

        if (!Files.exists(instancesDir)) {
            Files.createDirectories(instancesDir);
        }

        try (var paths = Files.list(instancesDir)) {
            paths.filter(Files::isDirectory)
                    .forEach(path -> {
                        try {
                            File file = path.resolve("config.json").toFile();

                            if (file.exists()) {
                                InstanceProfile profile = mapper.readValue(file, InstanceProfile.class);
                                Path normalizedPath = path.toAbsolutePath().normalize();
                                if (normalizeProperties(profile, normalizedPath)) {
                                    save(profile);
                                }
                                profiles.add(profile);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }

        return profiles;
    }

    public Optional<InstanceProfile> findById(String id) throws Exception {
        return loadAll().stream()
                .filter(profile -> profile.getId().equals(id))
                .findFirst();
    }

    public void save(InstanceProfile profile) throws Exception {

        Path dir = instancesDir.resolve(profile.getId());

        Files.createDirectories(dir);
        Files.createDirectories(dir.resolve("logs"));
        Files.createDirectories(dir.resolve("data"));

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                        dir.resolve("config.json").toFile(),
                        profile
                );
    }

    public Path resolvePropertiesPath(InstanceProfile profile) throws Exception {
        Path instanceDir = resolveInstanceDir(profile.getId());
        Path normalizedInstanceDir = instanceDir.toAbsolutePath().normalize();

        if (normalizeProperties(profile, normalizedInstanceDir)) {
            save(profile);
        }

        String properties = profile.getProperties();
        if (properties == null || properties.isBlank()) {
            throw new IOException("No properties file found for instance " + profile.getId());
        }

        Path resolved = normalizedInstanceDir.resolve(properties).normalize();
        if (!resolved.startsWith(normalizedInstanceDir) || !Files.isRegularFile(resolved)) {
            throw new IOException("Properties file not found for instance " + profile.getId() + ": " + properties);
        }

        return resolved;
    }

    public void delete(String id) throws IOException {
        Path dir = instancesDir.resolve(id);

        if (!Files.exists(dir)) {
            return;
        }

        try (var paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        }
    }

    public Path resolveInstanceDir(String id) {
        return instancesDir.resolve(id);
    }

    private boolean normalizeProperties(InstanceProfile profile, Path instanceDir) throws IOException {
        String configuredProperties = profile.getProperties();
        if (configuredProperties != null) {
            configuredProperties = configuredProperties.trim();
            if (configuredProperties.isEmpty()) {
                configuredProperties = null;
            }
        }

        if (configuredProperties != null) {
            Path configuredPath = instanceDir.resolve(configuredProperties).normalize();
            if (configuredPath.startsWith(instanceDir) && Files.isRegularFile(configuredPath)) {
                if (!configuredProperties.equals(profile.getProperties())) {
                    profile.setProperties(configuredProperties);
                    return true;
                }
                return false;
            }
        }

        Optional<Path> newestProperties = findNewestPropertiesFile(instanceDir);
        if (newestProperties.isPresent()) {
            String relativePath = instanceDir.relativize(newestProperties.get()).toString();
            if (!relativePath.equals(profile.getProperties())) {
                profile.setProperties(relativePath);
                return true;
            }
            return false;
        }

        if (profile.getProperties() != null) {
            profile.setProperties(null);
            return true;
        }

        return false;
    }

    private Optional<Path> findNewestPropertiesFile(Path instanceDir) throws IOException {
        try (var paths = Files.walk(instanceDir)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".properties"))
                    .max(Comparator.comparingLong(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }));
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        }
    }
}