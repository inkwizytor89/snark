package org.enoch.conductor.startproperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StartPropertiesRepository {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("<([A-Za-z0-9._-]+)>");
    private static final String DEFAULT_TEMPLATE_RESOURCE = "start-properties/start.properties";

    private final Path startPropertiesDir;

    public StartPropertiesRepository(@Value("${conductor.start-properties-dir:start-properties}") String startPropertiesDirPath) {
        this.startPropertiesDir = Path.of(startPropertiesDirPath);
    }

    @PostConstruct
    public void ensureDefaultTemplateExists() throws IOException {
        Files.createDirectories(startPropertiesDir);

        Path defaultTemplate = startPropertiesDir.resolve("start.properties");
        if (Files.notExists(defaultTemplate)) {
            Files.writeString(defaultTemplate, readDefaultTemplateContent(), StandardCharsets.UTF_8);
        }
    }

    private String readDefaultTemplateContent() throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(DEFAULT_TEMPLATE_RESOURCE)) {
            if (inputStream == null) {
                throw new IOException("Missing default template resource: " + DEFAULT_TEMPLATE_RESOURCE);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public List<StartPropertyTemplate> listTemplates() throws IOException {
        if (Files.notExists(startPropertiesDir)) {
            ensureDefaultTemplateExists();
        }

        List<StartPropertyTemplate> templates = new ArrayList<>();
        try (var paths = Files.list(startPropertiesDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".properties"))
                    .forEach(path -> {
                        try {
                            templates.add(new StartPropertyTemplate(
                                    path.getFileName().toString(),
                                    extractPlaceholders(Files.readString(path, StandardCharsets.UTF_8))
                            ));
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

        templates.sort((left, right) -> left.name().compareToIgnoreCase(right.name()));
        return templates;
    }

    public StartPropertyTemplate getTemplate(String templateName) throws IOException {
        Path templatePath = resolveTemplatePath(templateName);
        if (Files.notExists(templatePath) || !Files.isRegularFile(templatePath)) {
            return null;
        }

        return new StartPropertyTemplate(
                templatePath.getFileName().toString(),
                extractPlaceholders(Files.readString(templatePath, StandardCharsets.UTF_8))
        );
    }

    public List<String> copyTemplateToInstance(String templateName, Path instanceDir) throws IOException {
        Path templatePath = resolveTemplatePath(templateName);
        if (Files.notExists(templatePath) || !Files.isRegularFile(templatePath)) {
            throw new IOException("Template not found: " + templateName);
        }

        Files.createDirectories(instanceDir);
        Path serverProperties = instanceDir.resolve("server.properties");
        Files.copy(templatePath, serverProperties, StandardCopyOption.REPLACE_EXISTING);
        return extractPlaceholders(Files.readString(serverProperties, StandardCharsets.UTF_8));
    }

    public void applyPlaceholderValues(Path serverPropertiesPath, Map<String, String> values) throws IOException {
        String content = Files.readString(serverPropertiesPath, StandardCharsets.UTF_8);
        Set<String> placeholders = new LinkedHashSet<>(extractPlaceholders(content));

        for (String placeholder : placeholders) {
            if (!values.containsKey(placeholder)) {
                throw new IllegalArgumentException("Missing value for placeholder: " + placeholder);
            }
            String replacement = values.get(placeholder);
            content = content.replace("<" + placeholder + ">", replacement == null ? "" : replacement);
        }

        Files.writeString(serverPropertiesPath, content, StandardCharsets.UTF_8);
    }

    public Path getStartPropertiesDir() {
        return startPropertiesDir;
    }

    public List<String> extractPlaceholders(String content) {
        Set<String> placeholders = new LinkedHashSet<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }
        return new ArrayList<>(placeholders);
    }

    private Path resolveTemplatePath(String templateName) throws IOException {
        Path normalizedDir = startPropertiesDir.toAbsolutePath().normalize();
        Path templatePath = normalizedDir.resolve(templateName).normalize();
        if (!templatePath.startsWith(normalizedDir)) {
            throw new IOException("Invalid template name: " + templateName);
        }
        return templatePath;
    }
}
