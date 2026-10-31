package org.enoch.conductor.api;

import org.enoch.conductor.config.DeleteInstanceConfigsRequest;
import org.enoch.conductor.config.ConfigDefinitionsResponse;
import org.enoch.conductor.config.InstanceConfigResponse;
import org.enoch.conductor.config.InstanceConfigService;
import org.enoch.conductor.config.UpdateInstanceConfigsRequest;
import org.enoch.conductor.instance.InstanceProfile;
import org.enoch.conductor.instance.ProfileRepository;
import org.enoch.conductor.process.ProcessService;
import org.enoch.conductor.settings.ConductorSettings;
import org.enoch.conductor.startproperties.StartPropertiesRepository;
import org.enoch.conductor.startproperties.StartPropertyTemplate;
import org.enoch.common.status.StatusDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/instances")
@CrossOrigin(origins = "*")
public class InstanceController {

    private final ProcessService processService;
    private final ProfileRepository profileRepository;
    private final StartPropertiesRepository startPropertiesRepository;
    private final InstanceConfigService instanceConfigService;
    private final ConductorSettings conductorSettings;

    public InstanceController(ProcessService processService,
                              ProfileRepository profileRepository,
                              StartPropertiesRepository startPropertiesRepository,
                              InstanceConfigService instanceConfigService,
                              ConductorSettings conductorSettings) {
        this.processService = processService;
        this.profileRepository = profileRepository;
        this.startPropertiesRepository = startPropertiesRepository;
        this.instanceConfigService = instanceConfigService;
        this.conductorSettings = conductorSettings;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listInstances() throws Exception {
        List<InstanceProfile> profiles = profileRepository.loadAll();

        List<Map<String, Object>> response = profiles.stream()
                .map(profile -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", profile.getId());
                    item.put("autoStart", profile.isAutoStart());
                    item.put("databaseName", profile.getDatabaseName());
                    item.put("properties", profile.getProperties());
                    item.put("discordChannelId", profile.getDiscordChannelId());
                    boolean running = processService.isRunning(profile.getId());
                    item.put("running", running);
                    item.put("state", processService.getState(profile.getId()));
                    var runtime = processService.getRuntime(profile.getId());
                    item.put("pid", runtime != null ? runtime.getPid() : null);
                    item.put("serverStatus", processService.getLastServerStatus(profile.getId()));
                    return item;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("browserAutoOpen", conductorSettings.isBrowserAutoOpen());
        settings.put("instancesDir", conductorSettings.getInstancesDir());
        settings.put("startPropertiesDir", conductorSettings.getStartPropertiesDir());
        settings.put("templatesDir", conductorSettings.getTemplatesDir());
        settings.put("workerJar", conductorSettings.getWorkerJar());
        settings.put("autoStartIntervalMs", conductorSettings.getInstanceMonitorIntervalMs());
        settings.put("workerDisconnectTimeoutMs", conductorSettings.getWorkerDisconnectTimeoutMs());
        settings.put("serverPort", conductorSettings.getServerPort());
        settings.put("discordBotEnabled", conductorSettings.isDiscordBotEnabled());
        settings.put("discordBotToken", conductorSettings.getDiscordBotToken());
        return ResponseEntity.ok(settings);
    }

    @PostMapping("/settings")
    public ResponseEntity<Map<String, Object>> saveSettings(@RequestBody Map<String, Object> request) throws IOException {
        ConductorSettings next = conductorSettings.copy();
        Object browserAutoOpen = request.get("browserAutoOpen");
        if (browserAutoOpen != null) {
            next.setBrowserAutoOpen(Boolean.parseBoolean(String.valueOf(browserAutoOpen)));
        }
        Object instancesDir = request.get("instancesDir");
        if (instancesDir != null && String.valueOf(instancesDir).isBlank() == false) {
            next.setInstancesDir(String.valueOf(instancesDir).trim());
        }
        Object startPropertiesDir = request.get("startPropertiesDir");
        if (startPropertiesDir != null && String.valueOf(startPropertiesDir).isBlank() == false) {
            next.setStartPropertiesDir(String.valueOf(startPropertiesDir).trim());
        }
        Object templatesDir = request.get("templatesDir");
        if (templatesDir != null && String.valueOf(templatesDir).isBlank() == false) {
            next.setTemplatesDir(String.valueOf(templatesDir).trim());
        }
        Object workerJar = request.get("workerJar");
        if (workerJar != null) {
            next.setWorkerJar(String.valueOf(workerJar).trim());
        }
        Object interval = request.get("autoStartIntervalMs");
        if (interval != null) {
            next.setInstanceMonitorIntervalMs(Long.parseLong(String.valueOf(interval).trim()));
        }
        Object timeout = request.get("workerDisconnectTimeoutMs");
        if (timeout != null) {
            next.setWorkerDisconnectTimeoutMs(Long.parseLong(String.valueOf(timeout).trim()));
        }
        Object serverPort = request.get("serverPort");
        if (serverPort != null) {
            next.setServerPort(Integer.parseInt(String.valueOf(serverPort).trim()));
        }
        Object discordBotEnabled = request.get("discordBotEnabled");
        if (discordBotEnabled != null) {
            next.setDiscordBotEnabled(Boolean.parseBoolean(String.valueOf(discordBotEnabled)));
        }
        Object discordBotToken = request.get("discordBotToken");
        if (discordBotToken != null) {
            next.setDiscordBotToken(String.valueOf(discordBotToken).trim());
        }

        conductorSettings.apply(next);
        Map<String, Object> saved = new HashMap<>();
        saved.put("message", "Settings saved");
        saved.put("browserAutoOpen", conductorSettings.isBrowserAutoOpen());
        saved.put("instancesDir", conductorSettings.getInstancesDir());
        saved.put("startPropertiesDir", conductorSettings.getStartPropertiesDir());
        saved.put("templatesDir", conductorSettings.getTemplatesDir());
        saved.put("workerJar", conductorSettings.getWorkerJar());
        saved.put("autoStartIntervalMs", conductorSettings.getInstanceMonitorIntervalMs());
        saved.put("workerDisconnectTimeoutMs", conductorSettings.getWorkerDisconnectTimeoutMs());
        saved.put("serverPort", conductorSettings.getServerPort());
        saved.put("discordBotEnabled", conductorSettings.isDiscordBotEnabled());
        saved.put("discordBotToken", conductorSettings.getDiscordBotToken());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/templates")
    public ResponseEntity<List<StartPropertyTemplate>> listTemplates() throws Exception {
        return ResponseEntity.ok(startPropertiesRepository.listTemplates());
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, String>> startInstance(@PathVariable String id) throws Exception {
        InstanceProfile profile = profileRepository.loadAll().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            processService.start(profile);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage(),
                    "id", id
            ));
        }
        return ResponseEntity.ok(Map.of("message", "Instance started", "id", id));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, String>> stopInstance(@PathVariable String id) {
        processService.stop(id);
        return ResponseEntity.ok(Map.of("message", "Instance stopped", "id", id));
    }

    @PostMapping("/{id}/restart")
    public ResponseEntity<Map<String, String>> restartInstance(@PathVariable String id) throws Exception {
        InstanceProfile profile = profileRepository.loadAll().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            processService.restart(profile);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage(),
                    "id", id
            ));
        }
        return ResponseEntity.ok(Map.of("message", "Instance restarted", "id", id));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> getInstanceStatus(@PathVariable String id) throws Exception {
        InstanceProfile profile = profileRepository.loadAll().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> status = new HashMap<>();
        status.put("id", profile.getId());
        boolean running = processService.isRunning(id);
        status.put("running", running);
        status.put("state", processService.getState(id));
        status.put("autoStart", profile.isAutoStart());
        status.put("databaseName", profile.getDatabaseName());
        status.put("properties", profile.getProperties());
        status.put("discordChannelId", profile.getDiscordChannelId());
        var runtime = processService.getRuntime(id);
        status.put("pid", runtime != null ? runtime.getPid() : -1);

        if (running) {
            long previousUpdateAt = processService.getLastStatusUpdatedAt(id);
            boolean requestSent = false;
            try {
                requestSent = processService.requestStatus(id);
            } catch (Exception e) {
                System.err.println("Failed to request status for instance " + id + ": " + e.getMessage());
                status.put("statusMessage", "Status request failed: " + e.getMessage());
            }

            if (requestSent) {
                long deadline = System.currentTimeMillis() + 300;
                while (System.currentTimeMillis() < deadline
                        && processService.getLastStatusUpdatedAt(id) <= previousUpdateAt) {
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                if (processService.getLastStatusUpdatedAt(id) <= previousUpdateAt) {
                    status.put("statusMessage", "Timed out waiting for a status response from the instance.");
                }
            } else {
                if (!status.containsKey("statusMessage")) {
                    status.put("statusMessage", "WebSocket is not connected; status is unavailable.");
                }
            }
        }

        if (!status.containsKey("statusMessage")) {
            status.put("statusMessage", processService.getLastStatusMessage(id));
        }
        status.put("webSocketConnected", processService.isWorkerConnected(id));
        status.put("state", processService.getState(id));
        status.put("statusUpdatedAt", processService.getLastStatusUpdatedAt(id));
        status.put("serverStatus", processService.getLastServerStatus(id));

        return ResponseEntity.ok(status);
    }

    @GetMapping("/{id}/status/colonies")
    public ResponseEntity<?> getInstanceStatusColonies(@PathVariable String id,
                                                       @RequestParam(defaultValue = "0") int offset,
                                                       @RequestParam(defaultValue = "25") int limit) throws Exception {
        if (profileRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (offset < 0 || limit < 1 || limit > 25) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "offset must be non-negative and limit must be between 1 and 25"
            ));
        }
        try {
            StatusDetails details = processService.requestStatusDetails(id, offset, limit);
            return ResponseEntity.ok(details);
        } catch (IllegalStateException e) {
            String message = e.getMessage() == null ? "Status details request failed" : e.getMessage();
            return ResponseEntity.status(message.startsWith("Timed out") ? 504 : 409)
                    .body(Map.of("error", message));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to fetch status details: " + e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<CreateInstanceResponse> createInstance(@RequestBody CreateInstanceRequest request) throws Exception {
        String server = trimToNull(request.server());
        String login = trimToNull(request.login());
        String templateName = request.templateName();

        if (server == null || login == null) {
            return ResponseEntity.badRequest().build();
        }

        if (templateName == null || templateName.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String instanceId = server + "#" + login;

        StartPropertyTemplate template;
        try {
            template = startPropertiesRepository.getTemplate(templateName);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }

        if (template == null) {
            return ResponseEntity.badRequest().build();
        }

        if (profileRepository.findById(instanceId).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        List<String> placeholders = template.placeholders();
        if (placeholders.isEmpty()) {
            materializeInstance(instanceId, templateName, trimToNull(request.databaseName()), Map.of());
            return ResponseEntity.ok(new CreateInstanceResponse("Instance created", instanceId, placeholders));
        }

        return ResponseEntity.ok(new CreateInstanceResponse("Instance prepared", instanceId, placeholders));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @PostMapping("/{id}/placeholders")
    public ResponseEntity<Map<String, String>> applyPlaceholders(@PathVariable String id,
                                                                 @RequestBody ApplyPlaceholdersRequest request) throws Exception {
        String server = trimToNull(request.server());
        String login = trimToNull(request.login());
        String templateName = request.templateName();

        if (server == null || login == null || templateName == null || templateName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Missing instance metadata",
                    "id", id
            ));
        }

        String derivedId = server + "#" + login;
        if (!derivedId.equals(id)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Instance id mismatch",
                    "id", id
            ));
        }

        if (profileRepository.findById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Instance already exists",
                    "id", id
            ));
        }

        if (request.placeholders() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Missing placeholder values",
                    "id", id
            ));
        }

        try {
            materializeInstance(
                    id,
                    templateName,
                    trimToNull(request.databaseName()),
                    request.placeholders()
            );
            return ResponseEntity.ok(Map.of(
                    "message", "Instance created",
                    "id", id
            ));
        } catch (IOException | IllegalArgumentException e) {
            try {
                profileRepository.delete(id);
            } catch (IOException cleanupError) {
                e.addSuppressed(cleanupError);
            }
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage(),
                    "id", id
            ));
        }
    }

    private void materializeInstance(String id, String templateName, String databaseName, Map<String, String> placeholderValues) throws Exception {
        InstanceProfile profile = new InstanceProfile();
        profile.setId(id);
        profile.setAutoStart(true);
        profile.setDatabaseName(databaseName);

        try {
            Path instanceDir = profileRepository.resolveInstanceDir(id);
            startPropertiesRepository.copyTemplateToInstance(templateName, instanceDir);
            Path propertiesPath = instanceDir.resolve("server.properties");
            startPropertiesRepository.applyPlaceholderValues(propertiesPath, placeholderValues);
            profile.setProperties("server.properties");
            profileRepository.save(profile);
        } catch (Exception e) {
            try {
                profileRepository.delete(id);
            } catch (IOException cleanupError) {
                e.addSuppressed(cleanupError);
            }
            throw e;
        }
    }

    @PostMapping("/{id}/autostart/toggle")
    public ResponseEntity<Map<String, Object>> toggleAutoStart(@PathVariable String id) throws Exception {
        InstanceProfile profile = profileRepository.findById(id).orElse(null);

        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        profile.setAutoStart(!profile.isAutoStart());
        profileRepository.save(profile);

        return ResponseEntity.ok(Map.of(
                "message", "Auto-start updated",
                "id", id,
                "autoStart", profile.isAutoStart()
        ));
    }

    @PostMapping("/{id}/discord/channel")
    public ResponseEntity<Map<String, Object>> assignDiscordChannel(@PathVariable String id,
                                                                   @RequestBody Map<String, String> request) throws Exception {
        InstanceProfile profile = profileRepository.findById(id).orElse(null);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        String channelId = request == null ? null : request.get("channelId");
        if (channelId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Missing channelId",
                    "id", id
            ));
        }

        String normalized = channelId.trim();
        if (normalized.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Discord channel id cannot be empty",
                    "id", id
            ));
        }
        if (!org.enoch.conductor.discord.DiscordChannelValidator.isAllowedForNotification(normalized)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Discord channel id is invalid. Expected a numeric Discord channel id.",
                    "id", id
            ));
        }

        profile.setDiscordChannelId(normalized);
        profileRepository.save(profile);
        return ResponseEntity.ok(Map.of(
                "message", "Discord channel assigned",
                "id", id,
                "discordChannelId", normalized
        ));
    }

    @DeleteMapping("/{id}/discord/channel")
    public ResponseEntity<Map<String, Object>> clearDiscordChannel(@PathVariable String id) throws Exception {
        InstanceProfile profile = profileRepository.findById(id).orElse(null);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        profile.setDiscordChannelId(null);
        profileRepository.save(profile);
        return ResponseEntity.ok(Map.of(
                "message", "Discord channel cleared",
                "id", id,
                "discordChannelId", null
        ));
    }

    @GetMapping("/{id}/discord/channel")
    public ResponseEntity<Map<String, Object>> getDiscordChannel(@PathVariable String id) throws Exception {
        InstanceProfile profile = profileRepository.findById(id).orElse(null);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "id", id,
                "discordChannelId", profile.getDiscordChannelId()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteInstance(@PathVariable String id) throws Exception {
        InstanceProfile profile = profileRepository.findById(id).orElse(null);

        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        if (processService.isRunning(id)) {
            processService.stop(id);
        }

        try {
            profileRepository.delete(id);
        } catch (IOException e) {
            throw e;
        }

        return ResponseEntity.ok(Map.of(
                "message", "Instance deleted",
                "id", id
        ));
    }

    @GetMapping("/{id}/configs")
    public ResponseEntity<?> getInstanceConfigs(@PathVariable String id) throws Exception {
        InstanceProfile profile = profileRepository.findById(id).orElse(null);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            var configs = instanceConfigService.fetchConfigs(id);
            return ResponseEntity.ok(new InstanceConfigResponse(id, configs));
        } catch (RuntimeException e) {
            return mapConfigException(e);
        }
    }

    @GetMapping("/{id}/configs/definitions")
    public ResponseEntity<?> getConfigDefinitions(@PathVariable String id) throws Exception {
        InstanceProfile profile = profileRepository.findById(id).orElse(null);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            var definitions = instanceConfigService.fetchDefinitions(id);
            return ResponseEntity.ok(new ConfigDefinitionsResponse(id, definitions));
        } catch (RuntimeException e) {
            return mapConfigException(e);
        }
    }

    @PostMapping("/{id}/configs")
    public ResponseEntity<?> setInstanceConfigs(@PathVariable String id, @RequestBody UpdateInstanceConfigsRequest request) throws Exception {
        InstanceProfile profile = profileRepository.findById(id).orElse(null);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            var configs = instanceConfigService.setConfigs(id, request.configs() == null ? List.of() : request.configs());
            return ResponseEntity.ok(new InstanceConfigResponse(id, configs));
        } catch (RuntimeException e) {
            return mapConfigException(e);
        }
    }

    @DeleteMapping("/{id}/configs")
    public ResponseEntity<?> deleteInstanceConfigs(@PathVariable String id, @RequestBody DeleteInstanceConfigsRequest request) throws Exception {
        InstanceProfile profile = profileRepository.findById(id).orElse(null);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            var configs = instanceConfigService.deleteConfigs(id, request.configs() == null ? List.of() : request.configs());
            return ResponseEntity.ok(new InstanceConfigResponse(id, configs));
        } catch (RuntimeException e) {
            return mapConfigException(e);
        }
    }

    private ResponseEntity<Map<String, String>> mapConfigException(RuntimeException e) {
        String message = e.getMessage() == null ? "Unknown config error" : e.getMessage();

        if (message.contains("timed out")) {
            return ResponseEntity.status(504).body(Map.of("error", message));
        }

        if (message.contains("offline") || message.contains("disconnected")) {
            return ResponseEntity.status(409).body(Map.of("error", message));
        }

        if (message.startsWith("Config request failed:")) {
            return ResponseEntity.badRequest().body(Map.of("error", message));
        }

        return ResponseEntity.status(409).body(Map.of("error", message));
    }
}

