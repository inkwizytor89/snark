package org.enoch.conductor.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.enoch.common.command.CommandMessage;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ConfigEntry;
import org.enoch.conductor.ws.WorkerSocketHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class InstanceConfigService {

    private static final long REQUEST_TIMEOUT_SECONDS = 10L;

    private final InstanceConfigStateRepository stateRepository;
    private final WorkerSocketHandler workerSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, CompletableFuture<List<ConfigDefinition>>> pendingRequests = new ConcurrentHashMap<>();
    private final Map<String, String> pendingRequestInstances = new ConcurrentHashMap<>();

    public InstanceConfigService(InstanceConfigStateRepository stateRepository,
                                 WorkerSocketHandler workerSocketHandler) {
        this.stateRepository = stateRepository;
        this.workerSocketHandler = workerSocketHandler;
    }

    @PostConstruct
    void registerWithSocketHandler() {
        workerSocketHandler.setInstanceConfigService(this);
    }

    public List<ConfigDefinition> fetchConfigs(String instanceId) throws Exception {
        List<ConfigDefinition> result = sendConfigRequest(instanceId, "request:config:list");
        if (result != null) {
            stateRepository.saveConfigs(instanceId, result);
        }
        return result;
    }

    public List<ConfigDefinition> fetchDefinitions(String instanceId) throws Exception {
        List<ConfigDefinition> result = sendConfigRequest(instanceId, "request:config:definitions");
        if (result != null) {
            stateRepository.saveDefinitions(instanceId, result);
        }
        return result;
    }

    public List<ConfigDefinition> setConfigs(String instanceId, List<ConfigEntry> configs) throws Exception {
        List<ConfigDefinition> result = sendConfigRequest(instanceId, "request:config:set", configs);
        if (result != null) {
            stateRepository.saveConfigs(instanceId, result);
        }
        return result;
    }

    public void setModuleEntry(String instanceId, ConfigEntry config) throws Exception {
        sendConfigRequest(instanceId, "request:module:set", config);
    }

    public void updateConfigEntry(String instanceId, ConfigEntry config) throws Exception {
        List<ConfigDefinition> result = sendConfigRequest(instanceId, "request:config:set", List.of(config));
        if (result == null || result.isEmpty()) {
            return;
        }

        List<ConfigDefinition> currentConfigs = new ArrayList<>(stateRepository.getConfigs(instanceId));
        for (ConfigDefinition updated : result) {
            ConfigDefinition existing = currentConfigs.stream()
                    .filter(current -> Objects.equals(current.getKey(), updated.getKey()))
                    .findFirst()
                    .orElse(null);
            if (existing == null) {
                currentConfigs.add(updated);
            } else {
                existing.setValue(updated.getValue());
            }
        }
        stateRepository.saveConfigs(instanceId, currentConfigs);
    }

    public List<ConfigDefinition> deleteConfigs(String instanceId, List<ConfigEntry> configs) throws Exception {
        List<ConfigDefinition> result = sendConfigRequest(instanceId, "request:config:delete", configs);
        if (result != null) {
            stateRepository.saveConfigs(instanceId, result);
        }
        return result;
    }

    public void changeThreadModule(String instanceId, ConfigEntry term, String moduleName) throws Exception {
        sendConfigRequest(instanceId, "request:thread:change-module", Map.of("config", term, "module", moduleName));
    }

    public void deleteModule(String instanceId, ConfigEntry term) throws Exception {
        sendConfigRequest(instanceId, "request:module:delete", term);
    }

    public void deleteThread(String instanceId, ConfigEntry term) throws Exception {
        sendConfigRequest(instanceId, "request:thread:delete", term);
    }

    private List<ConfigDefinition> sendConfigRequest(String instanceId, String type) throws Exception {
        return sendConfigRequest(instanceId, type, null);
    }

    private List<ConfigDefinition> sendConfigRequest(String instanceId, String type, Object configs) throws Exception {
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<List<ConfigDefinition>> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        pendingRequestInstances.put(requestId, instanceId);

        try {
            CommandMessage message = new CommandMessage();
            message.setId(requestId);
            message.setType(type);
            message.setFrom("conductor");
            message.setTo(instanceId);
            message.setTimestamp(System.currentTimeMillis());
            message.setStatus(100);
            message.setPayload(configs);

            workerSocketHandler.send(instanceId, objectMapper.writeValueAsString(message));

            return future.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Config request timed out", e);
        } finally {
            pendingRequests.remove(requestId);
            pendingRequestInstances.remove(requestId);
        }
    }

    public void handleConfigResponse(CommandMessage response) {
        if (response == null || response.getId() == null) {
            return;
        }
        CompletableFuture<List<ConfigDefinition>> future = pendingRequests.get(response.getId());
        String expectedInstance = pendingRequestInstances.get(response.getId());
        if (future == null
                || expectedInstance == null
                || !expectedInstance.equals(response.getFrom())
                || !"conductor".equals(response.getTo())) {
            return;
        }

        if (response.getStatus() == 200) {
            List<ConfigDefinition> configs = extractConfigList(response.getPayload());
            future.complete(configs);
        } else {
            String errorMsg = response.getError() != null ? response.getError() : "Unknown error";
            future.completeExceptionally(new RuntimeException("Config request failed: " + errorMsg));
        }
    }

    @SuppressWarnings("unchecked")
    private List<ConfigDefinition> extractConfigList(Object payload) {
        if (payload == null) {
            return List.of();
        }

        if (payload instanceof Map<?, ?> payloadMap) {
            if (payloadMap.containsKey("configs")) {
                return objectMapper.convertValue(payloadMap.get("configs"), new TypeReference<List<ConfigDefinition>>() {});
            }
            if (payloadMap.containsKey("module")) {
                return List.of();
            }
        }
        return objectMapper.convertValue(payload, new TypeReference<List<ConfigDefinition>>() {});
    }

    public void clearInstanceState(String instanceId) {
        stateRepository.clear(instanceId);
    }

    public void cancelPendingRequests(String instanceId) {
        pendingRequestInstances.forEach((requestId, requestInstanceId) -> {
            if (instanceId.equals(requestInstanceId)) {
                CompletableFuture<List<ConfigDefinition>> future = pendingRequests.get(requestId);
                if (future != null && !future.isDone()) {
                    future.completeExceptionally(new RuntimeException("Instance disconnected"));
                }
            }
        });
    }
}
