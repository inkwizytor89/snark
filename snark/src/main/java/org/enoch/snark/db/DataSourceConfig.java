package org.enoch.snark.db;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.config.ConfigurationScheduledTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

    private final ConfigurationScheduledTask configurationScheduledTask;

    @Value("${database-name}")
    private String databaseName;

    @Bean
    public DataSource dataSource() {
        if(databaseName!=null) return postgresDataSource();
        else return h2DataSource();
    }

    private DriverManagerDataSource postgresDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/"+databaseName);
        dataSource.setUsername("postgres");
        dataSource.setPassword("");
        return dataSource;
    }

    private DriverManagerDataSource h2DataSource() {
        String databaseName = configurationScheduledTask.determineDatabase();
        throw new NotImplementedException("H2 Databsse not implemented yet for "+databaseName);
    }
}
