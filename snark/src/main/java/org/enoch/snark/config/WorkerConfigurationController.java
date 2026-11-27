package org.enoch.snark.config;

import org.enoch.common.config.ModuleDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/internal/configurations")
public class WorkerConfigurationController {

    private final ConfigService configService;
    private final String instanceId;

    public WorkerConfigurationController(ConfigService configService,
                                          @Value("${instanceId:${spring.application.name:snark}}") String instanceId) {
        this.configService = configService;
        this.instanceId = instanceId;
    }

    @GetMapping
    public WorkerConfigurationResponse getConfigurations(@RequestParam(required = false) String instanceId) {
        if (instanceId != null && !instanceId.isBlank() && !this.instanceId.equals(instanceId)) {
            throw new IllegalArgumentException("Instance ID mismatch");
        }

        List<ThreadDefinition> definitions = configService.listThreadDefinitions();
        List<ModuleDefinition> modules = configService.listModuleDefinitions();
        List<String> availableTypes = configService.availableThreadTypes();
        return new WorkerConfigurationResponse(this.instanceId, definitions, modules, availableTypes);
    }

    public record WorkerConfigurationResponse(
            String instanceId,
            List<ThreadDefinition> definitions,
            List<ModuleDefinition> modules,
            List<String> availableTypes
    ) {
    }
}
