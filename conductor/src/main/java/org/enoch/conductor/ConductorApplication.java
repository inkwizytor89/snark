package org.enoch.conductor;

import org.enoch.conductor.settings.ConductorSettings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class ConductorApplication {

    public static void main(String[] args) {
        Map<String, Object> defaults = new HashMap<>(ConductorSettings.resolveSpringDefaults(args));

        SpringApplication application = new SpringApplication(ConductorApplication.class);
        application.setDefaultProperties(defaults);
        application.run(args);
    }
}
