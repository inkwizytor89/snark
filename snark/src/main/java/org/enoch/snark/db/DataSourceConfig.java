package org.enoch.snark.db;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.config.ConfigurationScheduledTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

    private static final String DEFAULT_H2_DATABASE_FILE = "snark";

    private final ConfigurationScheduledTask configurationScheduledTask;

    @Value("${database-name:}")
    private String databaseName;

    @Value("${database-file:}")
    private String databaseFile;

    @Bean
    public DataSource dataSource() {
        if (StringUtils.isNotBlank(databaseName)) {
            return postgresDataSource();
        }
        return h2DataSource();
    }

    private DriverManagerDataSource postgresDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/" + databaseName);
        dataSource.setUsername("postgres");
        dataSource.setPassword("");
        return dataSource;
    }

    private DriverManagerDataSource h2DataSource() {
        String resolvedDatabaseFile = resolveH2DatabasePath();
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:file:" + resolvedDatabaseFile + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    private String resolveH2DatabasePath() {
        String configuredPath = StringUtils.trimToEmpty(databaseFile);
        Path databasePath = configuredPath.isEmpty()
                ? Path.of(System.getProperty("user.dir"), DEFAULT_H2_DATABASE_FILE)
                : Path.of(configuredPath);

        if (!databasePath.isAbsolute()) {
            databasePath = Path.of(System.getProperty("user.dir")).resolve(databasePath);
        }

        Path parentDirectory = databasePath.getParent();
        if (parentDirectory != null) {
            try {
                Files.createDirectories(parentDirectory);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create H2 database directory: " + parentDirectory, e);
            }
        }

        return databasePath.toAbsolutePath().normalize().toString();
    }
}
