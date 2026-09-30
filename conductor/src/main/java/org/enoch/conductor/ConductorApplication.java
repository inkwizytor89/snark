package org.enoch.conductor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class ConductorApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ConductorApplication.class);
        application.setDefaultProperties(resolveDirectoryDefaults(args));
        application.run(args);
    }

    private static Map<String, Object> resolveDirectoryDefaults(String[] args) {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("conductor.instances-dir", "instances");
        defaults.put("conductor.start-properties-dir", "start-properties");
        defaults.put("conductor.worker", "");

        for (String arg : args) {
            if (arg.startsWith("--instances-dir=")) {
                defaults.put("conductor.instances-dir", arg.substring("--instances-dir=".length()));
            } else if (arg.startsWith("--start-properties-dir=")) {
                defaults.put("conductor.start-properties-dir", arg.substring("--start-properties-dir=".length()));
            } else if (arg.startsWith("--worker=")) {
                defaults.put("conductor.worker", arg.substring("--worker=".length()));
            }
        }

        return defaults;
    }
}
