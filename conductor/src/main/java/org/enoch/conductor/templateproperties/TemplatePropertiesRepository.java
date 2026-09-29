package org.enoch.conductor.templateproperties;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class TemplatePropertiesRepository {

    private static final String RESOURCE_PATTERN = "classpath*:template-properties/*";

    private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
    private final Path templatesDir;

    public TemplatePropertiesRepository(@Value("${conductor.templates-dir:templates}") String templatesDirPath) {
        this.templatesDir = Path.of(templatesDirPath);
    }

    @PostConstruct
    public void ensureDefaultTemplatesExist() throws IOException {
        Files.createDirectories(templatesDir);

        for (Resource resource : resourceResolver.getResources(RESOURCE_PATTERN)) {
            if (!resource.isReadable()) {
                continue;
            }

            String filename = resource.getFilename();
            if (filename == null || filename.isBlank()) {
                continue;
            }

            Path target = templatesDir.resolve(filename);
            if (Files.notExists(target)) {
                try (InputStream inputStream = resource.getInputStream()) {
                    Files.copy(inputStream, target);
                }
            }
        }
    }

    public Path getTemplatesDir() {
        return templatesDir;
    }
}

